package javadash.game

import javadash.Main
import javadash.ui.Displayable
import javadash.ui.Scene
import javadash.util.Vector
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.Timer


class GameScene : Scene() {
    lateinit var timer: Timer
    private val player: Player
    private val refreshDelay: Int = 8
    private val gameObjectList = ArrayList<AbstractGameObject>()
    private val rigidBodyList = ArrayList<RigidBody>()

    init {
        // black background
        layers[9].addElement(
            javadash.ui.Rectangle(
                0,
                0,
                Main.MAIN_FRAME.windowDimension.width,
                Main.MAIN_FRAME.windowDimension.height,
                Color.BLACK
            )
        )

        // player
        player = Player(Vector(100, 100), Dimension(34, 34))
        layers[0].addElement(player)

        rebuildIndex()

        // key controller
        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
                player.keyTyped(e)
            }

            override fun keyPressed(e: KeyEvent?) {
                player.keyPressed(e)
                // pause
                if (e!!.keyCode == KeyEvent.VK_ESCAPE) {
                    if (timer.isRunning) {
                        timer.stop()
                    } else {
                        timer.restart()
                    }
                }
            }

            override fun keyReleased(e: KeyEvent?) {
                player.keyReleased(e)
            }
        })
    }

    override fun paint(g2d: Graphics2D) {
        // paint background before transformation
        layers[9].paint(g2d)
        val transform = g2d.transform
        transform.translate((-player.pos.x.toInt() + 300).toDouble(), 0.0)
        g2d.transform = transform
        paintWithoutBackground(g2d)
    }

    private fun rebuildIndex() {
        synchronized("gameObjectList") {
            gameObjectList.clear()
        }
        synchronized(rigidBodyList) {
            rigidBodyList.clear()
        }
        for (i in 1..8) {
            layers[i].elements.forEach {
                if (it is AbstractGameObject) {
                    indexGameObject(it)
                }
            }
        }
    }

    private fun indexGameObject(it: AbstractGameObject) {
        synchronized(gameObjectList) {
            gameObjectList.add(it)
        }
        if (it is RigidBody) {
            synchronized(rigidBodyList) {
                rigidBodyList.add(it)
            }
        }
    }

    override fun addElement(layer: Int, d: Displayable) {
        layers[layer].addElement(d)
        if (d is AbstractGameObject) {
            indexGameObject(d)
        }
    }

    fun start() {
        rebuildIndex()
        // add timer
        timer = Timer(refreshDelay) {
            detectCollision()
            update()
        }
        timer.start()
    }

    private fun update() {
        player.update(refreshDelay)
        // TODO in the future all objects will be updated
    }

    private fun detectCollision() {
        rigidBodyList.forEach {
            val event = it.detectCollision(player)
            if (event != null) {
                player.resolveCollision(event)
            }
        }
        // TODO in the future the objects will be indexed periodically for better performance
    }

    // TODO collision, game scene, zoning (comparing the player's x position and the objects' x positions)
}