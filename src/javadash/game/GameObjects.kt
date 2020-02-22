package javadash.game

import javadash.Main
import javadash.ui.Displayable
import javadash.ui.OptionPane
import javadash.util.Vector
import java.awt.*
import java.awt.event.KeyEvent
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * for all non player rigid body game objects
 */
interface RigidBody {
    fun detectCollision(player: Player): CollisionEvent?
}

/**
 * for all non-UI game objects
 */
abstract class AbstractGameObject(
    @Volatile var pos: Vector,
    var isFixed: Boolean = false,
    var isKiller: Boolean = false,
    var updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Displayable {
    @Volatile
    var vel = Vector.ZeroVector

    @Volatile
    var acc = Vector.ZeroVector

    abstract override fun paint(g2d: Graphics2D)
    open fun update(timeElapsed: Int) {
        updatingScript.accept(this, timeElapsed)
    }
}

class Array<out E : AbstractGameObject> {

}

/**
 * the player
 */
class Player(pos: Vector, val dimension: Dimension = Dimension(34, 34)) : AbstractGameObject(pos) {
    private var playerMode: PlayerMode = DefaultPlayerMode()

    init {
        acc = Vector(0, 10000)
        vel = Vector(600, 0)
    }

    override fun paint(g2d: Graphics2D) {
        g2d.color = Color.RED
        g2d.fillRect(pos.x.toInt(), pos.y.toInt(), dimension.width, dimension.height)
    }

    /**
     * update the player according to the game mode
     */
    override fun update(timeElapsed: Int) {
        playerMode.update(this, timeElapsed)
    }

    fun resolveCollision(collisionEvent: CollisionEvent) {
        playerMode.resolveCollision(collisionEvent)
    }

    fun keyPressed(e: KeyEvent?) {
        playerMode.keyPressed(e)
    }

    fun keyReleased(e: KeyEvent?) {
        playerMode.keyReleased(e)
    }

    fun keyTyped(e: KeyEvent?) {
        playerMode.keyTyped(e)
    }

    fun kill() {
        Main.MAIN_FRAME.resetGameScene()
        System.gc()
        //OptionPane.showMessageDialog("Failed")
    }
}

open class Rectangle(
    x: Int,
    y: Int,
    var width: Int,
    var height: Int,
    fixed: Boolean = true,
    killer: Boolean = false,
    val color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    AbstractGameObject(Vector(x, y), fixed, killer, updatingScript) {
    override fun paint(g2d: Graphics2D) {
        g2d.color = color
        g2d.fillRect(pos.x.toInt(), pos.y.toInt(), width, height)
    }
}

/**
 * segment of the floor, top / left collision detections only
 */
class GroundSegment(
    x: Int, y: Int, width: Int, height: Int, color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Rectangle(x, y, width, height, true, false, color, updatingScript), RigidBody {
    private val decorationShape = java.awt.Rectangle(x, y, width, if (height < 200) height else 200)
    private val decorationGradientPaint = GradientPaint(
        x.toFloat(),
        y.toFloat(), Color.BLACK, x.toFloat(), (if (height < 200) y + height else y + 200).toFloat(), this.color
    )

    override fun paint(g2d: Graphics2D) {
        super.paint(g2d)
        // some more decorations

        g2d.paint = decorationGradientPaint
        g2d.fill(decorationShape)

        g2d.color = Color.WHITE
        g2d.drawRect(pos.x.toInt(), pos.y.toInt(), width, height)
    }

    override fun detectCollision(player: Player): CollisionEvent? {
        val pos = player.pos
        val dimension = player.dimension
        // top
        if (pos.x + dimension.width >= this.pos.x && pos.x <= this.pos.x + this.width) {
            if (pos.y + dimension.height >= this.pos.y && pos.y + dimension.height <= this.pos.y + 25) {
                return CollisionEvent(player, CollisionSide.TOP, this)
            }
        }
        // left
        if (pos.y + dimension.height >= this.pos.y && pos.y <= this.pos.y + this.height) {
            if (pos.x + dimension.width >= this.pos.x && pos.x <= this.pos.x + this.width) {
                return CollisionEvent(player, CollisionSide.LEFT, this)
            }
        }

        return null
    }
}

/**
 * segment of the ceiling, bottom / left collision detections only
 */
class CeilingSegment(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Rectangle(x, y, width, height, true, false, color, updatingScript), RigidBody {
    override fun detectCollision(player: Player): CollisionEvent? {
        val pos = player.pos
        val dimension = player.dimension

        // bottom
        if (pos.x + dimension.width >= this.pos.x && pos.x <= this.pos.x + this.width) {
            if (pos.y <= this.pos.y + this.height && pos.y >= this.pos.y + height - 10) {
                return CollisionEvent(player, CollisionSide.BOTTOM, this)
            }
        }
        // left
        if (pos.y + dimension.height >= this.pos.y && pos.y <= this.pos.y + this.height) {
            if (pos.x + dimension.width >= this.pos.x && pos.x <= this.pos.x + this.width) {
                return CollisionEvent(player, CollisionSide.LEFT, this)
            }
        }
        return null
    }
}

class Square(
    x: Int,
    y: Int,
    color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Rectangle(x, y, 34, 34, true, false, color, updatingScript), RigidBody {
    private val top: Polygon = Polygon()
    private val left: Polygon = Polygon()
    private val bottom: Polygon = Polygon()
    private val right: Polygon = Polygon()

    init {
        top.addPoint(x, y)
        top.addPoint(x + 10, y + 10)
        top.addPoint(x + width - 10, y + 10)
        top.addPoint(x + width, y)
        top.addPoint(x, y)
        left.addPoint(x, y)
        left.addPoint(x + 10, y + 10)
        left.addPoint(x + 10, y + height - 10)
        left.addPoint(x, y + height)
        left.addPoint(x, y)
        bottom.addPoint(x, (y + height))
        bottom.addPoint((x + width), (y + height))
        bottom.addPoint((x + width - 10), (y + height - 10))
        bottom.addPoint((x + 10), (y + height - 10))
        bottom.addPoint(x, (y + height))
        right.addPoint((x + width), y)
        right.addPoint((x + width), (y + height))
        right.addPoint((x + width - 10), (y + height - 10))
        right.addPoint((x + width - 10), (y + 10))
        right.addPoint((x + width), y)
    }

    override fun paint(g2d: Graphics2D) {
        super.paint(g2d)
        // decoration

        val x = pos.x.toFloat()
        val y = pos.y.toFloat()

        // side note: I just realized that there is probably no need to back up the paint as every time a gradient is
        // used a new paint is set, plus the fact that it does not affect the color
        // backup paint
        //val originalPaint = g2d.paint

        // top
        g2d.paint = GradientPaint(x, y, Color.BLACK, x, y + 8, this.color)
        g2d.fill(top)

        // left
        g2d.paint = GradientPaint(x, y, Color.BLACK, x + 8, y, this.color)
        g2d.fill(left)

        // bottom
        g2d.paint = GradientPaint(x, y + height, Color.BLACK, x, y + height - 8, this.color)
        g2d.fill(bottom)

        // right
        g2d.paint = GradientPaint(x + width, y, Color.BLACK, x + width - 10, y, this.color)
        g2d.fill(right)

        // restore paint
        //g2d.paint = originalPaint

        // white border
        g2d.color = Color.WHITE
        g2d.drawRect(x.toInt(), y.toInt(), width, height)
    }

    override fun detectCollision(player: Player): CollisionEvent? {
        val pos = player.pos
        val dimension = player.dimension
        // top
        if (pos.x + dimension.width >= this.pos.x && pos.x <= this.pos.x + this.width) {
            if (pos.y + dimension.height >= this.pos.y && pos.y + dimension.height <= this.pos.y + 25) {
                return CollisionEvent(player, CollisionSide.TOP, this)
            }
        }
        // bottom
        if (pos.x + dimension.width >= this.pos.x && pos.x <= this.pos.x + this.width) {
            if (pos.y <= this.pos.y + this.height && pos.y >= this.pos.y + height - 20) {
                return CollisionEvent(player, CollisionSide.BOTTOM, this)
            }
        }
        // left
        if (pos.y + dimension.height >= this.pos.y && pos.y <= this.pos.y + this.height) {
            if (pos.x + dimension.width >= this.pos.x && pos.x <= this.pos.x + this.width) {
                return CollisionEvent(player, CollisionSide.LEFT, this)
            }
        }
        return null
    }
}

/**
 * for up facing triangles, its position is defined as the coordinates of its bottom left corner
 * for down facing triangles, its position is defined as the coordinates of its top left corner
 */
class Triangle(
    x: Int,
    y: Int,
    private val faceUp: Boolean = true,
    color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Rectangle(x, y, 34, 34, true, true, color, updatingScript), RigidBody {
    private val triangle: Polygon = Polygon()

    init {
        if (faceUp) {
            triangle.addPoint(pos.x.toInt(), pos.y.toInt())
            triangle.addPoint((pos.x + width).toInt(), pos.y.toInt())
            triangle.addPoint((pos.x + width / 2).toInt(), (pos.y - height).toInt())
            triangle.addPoint(pos.x.toInt(), pos.y.toInt())
        } else {
            triangle.addPoint(pos.x.toInt(), pos.y.toInt())
            triangle.addPoint((pos.x + width).toInt(), pos.y.toInt())
            triangle.addPoint((pos.x + width / 2).toInt(), (pos.y + height).toInt())
            triangle.addPoint(pos.x.toInt(), pos.y.toInt())
        }
    }

    override fun paint(g2d: Graphics2D) {
        g2d.paint = if (faceUp) GradientPaint(
            pos.x.toFloat(), (pos.y - height).toFloat(), Color.BLACK,
            pos.x.toFloat(), pos.y.toFloat(), this.color
        ) else GradientPaint(
            pos.x.toFloat(), (pos.y + height).toFloat(), Color.BLACK,
            pos.x.toFloat(), pos.y.toFloat(), this.color
        )
        g2d.fill(triangle)
        // white boundary
        g2d.color = Color.WHITE
        g2d.draw(triangle)
    }

    override fun detectCollision(player: Player): CollisionEvent? {
        val x = player.pos.x.toInt()
        val y = player.pos.y.toInt()
        val width = player.dimension.width
        val height = player.dimension.height
        if (triangle.intersects(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())) {
            return CollisionEvent(player, CollisionSide.LEFT, this)
        }
        return null
    }
}

// TODO other elements
// TODO movable rectangles with changing colors