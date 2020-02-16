package javadash.ui

import javadash.Main
import java.awt.Color
import java.awt.Font
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

open class OkScene(private val previousScene: Scene) : Scene() {
    init {
        val dimension = Main.MAIN_FRAME.windowDimension
        // background
        layers[9].addElement(Rectangle(0, 0, dimension.width, dimension.height, Color.BLACK))
        // buttons
        val okBtn = Button(
            dimension.width - 100,
            dimension.height - 45,
            80,
            25,
            "OK",
            textLocationAdditionalOffset = Point(-7, 0)
        )
        val okBtnAction = Runnable {
            Main.MAIN_FRAME.activeScene = previousScene
        }
        okBtn.addActionListener(okBtnAction)
        layers[0].addElement(okBtn)

        // listening for keys
        // ESC for Cancel, ENTER for OK
        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {}
            override fun keyPressed(e: KeyEvent?) {}
            override fun keyReleased(e: KeyEvent?) {
                when (e!!.keyCode) {
                    KeyEvent.VK_ENTER -> okBtnAction.run()
                }
            }
        })
    }
}

open class OkCancelScene(
    private val previousScene: Scene,
    private val okBtnAction: Runnable,
    title: String,
    message: String
) : Scene() {

    init {
        val dimension = Main.MAIN_FRAME.windowDimension
        // background
        layers[9].addElement(Rectangle(0, 0, dimension.width, dimension.height, Color.BLACK))
        // buttons
        val okBtn = Button(
            dimension.width - 200,
            dimension.height - 45,
            80,
            25,
            "OK",
            textLocationAdditionalOffset = Point(-7, 0)
        )
        okBtn.addActionListener(okBtnAction)
        layers[0].addElement(okBtn)

        val cancelBtn = Button(
            dimension.width - 100,
            dimension.height - 45,
            80,
            25,
            "Cancel",
            textLocationAdditionalOffset = Point(-3, 0)
        )
        val cancelBtnAction = Runnable {
            Main.MAIN_FRAME.activeScene = previousScene
        }
        cancelBtn.addActionListener(cancelBtnAction)
        layers[0].addElement(cancelBtn)

        // listening for keys
        // ESC for Cancel, ENTER for OK
        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {}
            override fun keyPressed(e: KeyEvent?) {}
            override fun keyReleased(e: KeyEvent?) {
                when (e!!.keyCode) {
                    KeyEvent.VK_ESCAPE -> cancelBtnAction.run()
                    KeyEvent.VK_ENTER -> okBtnAction.run()
                }
            }
        })

        // show the title and the message
        val lblTitle = Label(80, 150, title, font = Font("Arial", Font.PLAIN, 80))
        val lblMessage = Label(80, 200, message, font = Font("Arial", Font.PLAIN, 20))
        layers[0].addAll(lblTitle, lblMessage)
    }
}