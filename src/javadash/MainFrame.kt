package javadash

import javadash.ui.*
import javadash.ui.Button
import javadash.ui.Rectangle
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import javax.swing.*
import kotlin.system.exitProcess


class MainFrame : JFrame() {
    val windowDimension: Dimension
    private val timer: Timer
    var isRunning: Boolean
        private set
    var activeScene: Scene = Scene()
    var previousMouseLocation: Point = MouseInfo.getPointerInfo().location
    private val bf: BufferedImage
    private val canvas: Canvas

    override fun paint(g: Graphics) {
        paint(g as Graphics2D)
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
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent) {}
            override fun windowClosing(e: WindowEvent) {
                if (activeScene is OkCancelScene) {
                    exitProcess(0)
                } else {
                    activeScene = OkCancelScene(activeScene, Runnable {
                        exitProcess(0)
                    }, "Alert", "Are you sure you want to exit?")
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
        this.isRunning = true
        val timer = ActionListener {
            /*
            this.paint(bf.graphics)
            val g2d = this.graphics as Graphics2D

            g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            )
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            g2d.drawImage(bf, 0, 0, windowDimension.width, windowDimension.height, null)

             */

            paint(canvas.bufferStrategy.drawGraphics)
            canvas.bufferStrategy.show()
        }
        this.timer = Timer(33, timer)
        this.timer.start()
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
        // test
        //this.activeScene.addElement(1, RectangularElement(0, 0, 100, 100, Color.RED))
        //this.activeScene.addElement(0, RectangularElement(25, 25, 50, 50, Color.BLACK))
        activeScene.addElement(9, Rectangle(0, 0, windowDimension.width, windowDimension.height, Color.BLACK))
        val btn = Button(400, 500, 80, 25, "Hello")
        btn.addActionListener(Runnable {
            JOptionPane.showMessageDialog(null, "Hello World")
        })
        activeScene.addElement(0, btn)
    }
}