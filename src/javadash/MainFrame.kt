package javadash

import javadash.ui.Button
import javadash.ui.Rectangle
import javadash.ui.Scene
import javadash.ui.getMouseLocation
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.Timer
import javax.swing.WindowConstants
import kotlin.system.exitProcess

class MainFrame : JFrame() {
    private val windowDimension: Dimension
    private val timer: Timer
    var isRunning: Boolean
        private set
    var activeScene: Scene = Scene()
    var previousMouseLocation: Point = MouseInfo.getPointerInfo().location

    override fun paint(g: Graphics) {
        super.paint(g)
        paint(g as Graphics2D)
    }

    private fun paint(g: Graphics2D) {
        activeScene.paint(g)
        previousMouseLocation = getMouseLocation()
    }

    init {
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent) {}
            override fun windowClosing(e: WindowEvent) {
                if (JOptionPane.showConfirmDialog(
                        null,
                        "Would you like to exit?",
                        "Exit",
                        JOptionPane.YES_NO_OPTION
                    ) == JOptionPane.YES_OPTION
                ) exitProcess(0)
            }

            override fun windowClosed(e: WindowEvent) {}
            override fun windowIconified(e: WindowEvent) {}
            override fun windowDeiconified(e: WindowEvent) {}
            override fun windowActivated(e: WindowEvent) {}
            override fun windowDeactivated(e: WindowEvent) {}
        })
        this.extendedState = Frame.MAXIMIZED_BOTH
        this.isUndecorated = true
        this.isVisible = true
        this.windowDimension = Dimension(width, height)
        this.isRunning = true
        val timer = ActionListener { this.paint(super.getGraphics()) }
        this.timer = Timer(50, timer)
        this.timer.start()

        // test
        //this.activeScene.addElement(1, RectangularElement(0, 0, 100, 100, Color.RED))
        //this.activeScene.addElement(0, RectangularElement(25, 25, 50, 50, Color.BLACK))
        activeScene.addElement(9, Rectangle(0, 0, windowDimension.width, windowDimension.height, Color.WHITE))
        activeScene.addElement(0, Button(400, 500, 80, 25, "Hello"))
    }
}