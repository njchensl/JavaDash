package javadash

import javadash.game.CeilingSegment
import javadash.game.GameScene
import javadash.game.GroundSegment
import javadash.ui.*
import javadash.ui.Rectangle
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.*
import javax.swing.Timer
import kotlin.system.exitProcess


class MainFrame : JFrame() {
    val windowDimension: Dimension
    private val _scenes = Stack<Scene>()
    private val timer: Timer
    var activeScene: Scene
        get() {
            return _scenes.peek()
        }
        set(value) {
            if (_scenes.size >= 1) {
                if (_scenes.peek() != value) {
                    _scenes.push(value)
                }
            } else {
                _scenes.push(value)
            }
        }
    var previousMouseLocation: Point = MouseInfo.getPointerInfo().location
    private val bf: BufferedImage
    private val canvas: Canvas
    private var paintTime = 100L

    override fun paint(g: Graphics) {
        paint(g as Graphics2D)
    }

    fun popScene() {
        if (_scenes.size > 1) {
            _scenes.pop()
        }
    }

    private fun paint(g: Graphics2D) {
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        activeScene.paint(g)
        previousMouseLocation = getMouseLocation()
    }

    init {
        val blankScene = Scene()
        blankScene.addElement(9, Rectangle(0, 0, Int.MAX_VALUE, Int.MAX_VALUE, Color.BLACK))
        activeScene = blankScene
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent) {}
            override fun windowClosing(e: WindowEvent) {
                if (activeScene !is OkCancelScene) {
                    OptionPane.showOkCancelDialog(
                        Runnable { exitProcess(0) },
                        message = "Are you sure you want to exit?"
                    )
                }
            }

            override fun windowClosed(e: WindowEvent) {}
            override fun windowIconified(e: WindowEvent) {}
            override fun windowDeiconified(e: WindowEvent) {}
            override fun windowActivated(e: WindowEvent) {}
            override fun windowDeactivated(e: WindowEvent) {}
        })
        this.extendedState = Frame.MAXIMIZED_BOTH
        this.isUndecorated = true
        canvas = Canvas()
        this.add(canvas)
        this.isVisible = true
        canvas.isFocusable = true
        this.windowDimension = Dimension(width, height)
        canvas.preferredSize = windowDimension
        canvas.createBufferStrategy(2)
        this.pack()
        this.bf = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val canvasBufferStrategy = canvas.bufferStrategy
        val timer = ActionListener {
            val time = System.currentTimeMillis()
            paint(canvasBufferStrategy.drawGraphics)
            canvasBufferStrategy.show()
            paintTime = System.currentTimeMillis() - time
        }
        this.timer = Timer(5, timer)
        this.timer.start()
        // drawing performance monitoring thread
        Thread {
            while (true) {
                Thread.sleep(paintTime)
                val framerateExpected = 1000 / (paintTime + 0.000000001)
                val framerateActual = 1000 / this.timer.delay
                if (framerateExpected < framerateActual - 20) {
                    this.timer.delay++
                } else if (framerateExpected > framerateActual + 50 && this.timer.delay > 4) {
                    this.timer.delay--
                }
                //println("Framerate: " + 1000 / this.timer.delay)
            }
        }.start()
        canvas.addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent?) {
                activeScene.mouseReleased(e)
            }

            override fun mouseEntered(e: MouseEvent?) {}
            override fun mouseClicked(e: MouseEvent?) {
                // transfer this interrupt downstream
                activeScene.mouseClicked(e)
            }

            override fun mouseExited(e: MouseEvent?) {}
            override fun mousePressed(e: MouseEvent?) {
                activeScene.mousePressed(e)
            }
        })
        canvas.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
                activeScene.keyTyped(e)
            }

            override fun keyPressed(e: KeyEvent?) {
                activeScene.keyPressed(e)
            }

            override fun keyReleased(e: KeyEvent?) {
                activeScene.keyReleased(e)
            }
        })
        /*
        // test
        //this.activeScene.addElement(1, RectangularElement(0, 0, 100, 100, Color.RED))
        //this.activeScene.addElement(0, RectangularElement(25, 25, 50, 50, Color.BLACK))
        activeScene.addElement(9, Rectangle(0, 0, windowDimension.width, windowDimension.height, Color.BLACK))
        val btn = Button(400, 500, 80, 25, "Hello")
        btn.addActionListener(Runnable {
            JOptionPane.showMessageDialog(null, "Hello World")
        })
        activeScene.addElement(0, btn)
         */

        Thread {
            Thread.sleep(1000)
            activeScene = GameScene()
            var gs = GroundSegment(0, 500, 1000, 1000)
            gs.color = Color.BLUE
            activeScene.addElement(8, gs)

            gs = GroundSegment(1000, 400, 100, 1000)
            gs.color = Color.GREEN
            activeScene.addElement(8, gs)

            gs = GroundSegment(1100, 300, 1000, 1000)
            gs.color = Color.MAGENTA
            activeScene.addElement(8, gs)

            val cs = CeilingSegment(500, -30, 300, 400)
            cs.color = Color.BLUE
            activeScene.addElement(8, cs)

            (activeScene as GameScene).start()
        }.start()

    }
}