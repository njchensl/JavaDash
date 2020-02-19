package javadash.ui

import javadash.Main
import javadash.game.GameScene
import java.awt.*
import java.awt.event.*

/**
 * checks the parameter for illegal type
 *
 * @param d the type to check
 */
fun checkParameterNotOfIllegalType(d: Displayable) {
    if (d is SceneLayer || d is Scene || d is GameScene) {
        throw UnsupportedElementException()
    }
}

/**
 * @return the location of the mouse as a point
 */
fun getMouseLocation(): Point {
    return MouseInfo.getPointerInfo().location;
}

/**
 * all objects that can be drawn on the screen should implement this interface
 */
interface Displayable {
    fun paint(g2d: Graphics2D)
}

/**
 * objects that can interact with the mouse should implement this interface
 */
interface CanInteractWithMouse {
    fun mouseIsInside(): Boolean
    fun mouseHasEntered(): Boolean
    fun mouseHasExited(): Boolean
    fun checkMousePosition()
    fun mouseClicked(e: MouseEvent?)
    fun mousePressed(e: MouseEvent?)
    fun mouseReleased(e: MouseEvent?)
}

/**
 * All UI elements inherit from this class
 */
abstract class AbstractUiElement(var x: Int, var y: Int) : Displayable {
    abstract override fun paint(g2d: Graphics2D)
}

/**
 * A scene layer is a collection of scene elements
 */
class SceneLayer : Displayable {
    val elements: MutableList<Displayable>

    init {
        elements = ArrayList()
    }

    override fun paint(g2d: Graphics2D) {
        synchronized(elements) {
            elements.forEach {
                it.paint(g2d)
            }
        }
    }

    fun addElement(d: Displayable) {
        checkParameterNotOfIllegalType(d)
        synchronized(elements) {
            elements.add(d)
        }
    }

    fun addAll(vararg d: Displayable) {
        synchronized(elements) {
            elements.addAll(d)
        }
    }

    fun mouseClicked(e: MouseEvent?) {
        // transfer the event downstream
        synchronized(elements) {
            elements.forEach {
                if (it is CanInteractWithMouse) {
                    it.mouseClicked(e)
                }
            }
        }
    }

    fun mousePressed(e: MouseEvent?) {
        synchronized(elements) {
            elements.forEach {
                if (it is CanInteractWithMouse) {
                    it.mousePressed(e)
                }
            }
        }
    }

    fun mouseReleased(e: MouseEvent?) {
        synchronized(elements) {
            elements.forEach {
                if (it is CanInteractWithMouse) {
                    it.mouseReleased(e)
                }
            }
        }
    }
}

class UnsupportedElementException : Exception()

/**
 * A scene is a collection of scene layers
 */
open class Scene : Displayable {
    protected val layers = Array(10) {
        return@Array SceneLayer()
    }
    private val keyListeners = ArrayList<KeyListener>()

    override fun paint(g2d: Graphics2D) {
        layers.reversed().forEach {
            it.paint(g2d)
        }
    }

    fun paintWithoutBackground(g2d: Graphics2D) {
        for (i in 8 downTo 0) {
            layers[i].paint(g2d)
        }
    }

    fun getLayer(x: Int): SceneLayer {
        return layers[x]
    }

    open fun addElement(layer: Int, d: Displayable) {
        layers[layer].addElement(d)
    }

    fun mouseClicked(e: MouseEvent?) {
        // transfer the event downstream to the layers
        layers.forEach {
            it.mouseClicked(e)
        }
    }

    fun mousePressed(e: MouseEvent?) {
        layers.forEach {
            it.mousePressed(e)
        }
    }

    fun mouseReleased(e: MouseEvent?) {
        layers.forEach {
            it.mouseReleased(e)
        }
    }

    fun keyPressed(e: KeyEvent?) {
        keyListeners.forEach {
            it.keyPressed(e)
        }
    }

    fun keyTyped(e: KeyEvent?) {
        keyListeners.forEach {
            it.keyTyped(e)
        }
    }

    fun keyReleased(e: KeyEvent?) {
        keyListeners.forEach {
            it.keyReleased(e)
        }
    }

    fun addKeyListener(keyListener: KeyListener) {
        keyListeners.add(keyListener)
    }

    fun removeAllKeyListeners() {
        keyListeners.clear()
    }

    fun getKeyListeners(): Array<KeyListener> {
        return keyListeners.toTypedArray()
    }
}

open class Rectangle(
    x: Int,
    y: Int,
    var width: Int,
    var height: Int,
    var backgroundColor: Color? = null
) : AbstractUiElement(x, y), Displayable {

    override fun paint(g2d: Graphics2D) {
        // draw background
        if (backgroundColor != null) {
            g2d.color = backgroundColor
            g2d.fillRect(x, y, width, height)
        }
    }
}

open class Button(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    var text: String = "",
    backgroundColor: Color = Color.LIGHT_GRAY,
    var pressedColor: Color = Color.GRAY,
    var fontColor: Color = Color.BLACK,
    var font: Font = Font("Arial", Font.PLAIN, 15),
    var textLocationOffset: Point = Point(
        (width / 2 - text.length * font.size / 3.9 + 3).toInt(),
        height / 2 + font.size / 3 + 1
    ),
    var textLocationAdditionalOffset: Point = Point(0, 0)
) : Rectangle(x, y, width, height, backgroundColor), Displayable, CanInteractWithMouse {

    var mouseListeners: MutableList<MouseListener> = ArrayList()
    protected var pressed = false

    override fun paint(g2d: Graphics2D) {
        if (backgroundColor != null) {
            g2d.color = if (!mouseIsInside()) backgroundColor else Color(
                backgroundColor!!.red - 50,
                backgroundColor!!.green - 50,
                backgroundColor!!.blue - 50
            )
            if (pressed) {
                g2d.color = pressedColor
            }
            g2d.fillRect(x, y, width, height)
        }
        g2d.color = fontColor
        g2d.font = font
        g2d.drawString(
            text,
            x + textLocationOffset.x + textLocationAdditionalOffset.x,
            y + textLocationOffset.y + textLocationAdditionalOffset.y
        )
    }

    override fun mouseIsInside(): Boolean {
        val p = getMouseLocation()
        return checkPointInsideElement(p)
    }

    private fun checkPointInsideElement(p: Point): Boolean {
        return p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + height
    }

    override fun mouseHasEntered(): Boolean {
        return checkPointInsideElement(getMouseLocation()) && !checkPointInsideElement(
            Main.MAIN_FRAME.previousMouseLocation
        )
    }

    override fun mouseHasExited(): Boolean {
        return !checkPointInsideElement(getMouseLocation()) && checkPointInsideElement(
            Main.MAIN_FRAME.previousMouseLocation
        )
    }

    override fun checkMousePosition() {
    }

    override fun mouseClicked(e: MouseEvent?) {
        if (mouseIsInside()) {
            // invoke the mouse listeners
            mouseListeners.forEach {
                it.mouseClicked(e)
            }
        }
    }

    override fun mousePressed(e: MouseEvent?) {
        if (mouseIsInside()) {
            pressed = true
            // invoke the mouse listeners
            mouseListeners.forEach {
                it.mousePressed(e)
            }
        }
    }

    override fun mouseReleased(e: MouseEvent?) {
        pressed = false
        if (mouseIsInside()) {
            // invoke the mouse listeners
            mouseListeners.forEach {
                it.mouseReleased(e)
            }
        }
    }

    fun addMouseListener(ml: MouseListener) {
        mouseListeners.add(ml)
    }

    fun addActionListener(al: Runnable) {
        mouseListeners.add(object : MouseListener {
            override fun mouseReleased(e: MouseEvent?) {}
            override fun mouseEntered(e: MouseEvent?) {}
            override fun mouseClicked(e: MouseEvent?) {
                al.run()
            }

            override fun mouseExited(e: MouseEvent?) {}
            override fun mousePressed(e: MouseEvent?) {}
        })
    }

    fun removeAllMouseListeners() {
        mouseListeners.clear()
    }

    fun getMouseListeners(): Array<MouseListener> {
        return mouseListeners.toTypedArray()
    }
}

class Label(
    x: Int,
    y: Int,
    var text: String = "",
    var fontColor: Color = Color.WHITE,
    var font: Font = Font("Arial", Font.PLAIN, 15)
) : AbstractUiElement(x, y) {

    override fun paint(g2d: Graphics2D) {
        g2d.color = fontColor
        g2d.font = font
        g2d.drawString(text, x, y)
    }
}