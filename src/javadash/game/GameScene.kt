package javadash.game

import javadash.MainFrame
import javadash.ui.Displayable
import javadash.ui.ImageView
import javadash.ui.Scene
import javadash.util.Vector
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Image
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.Timer


class GameScene(private val backgroundImage: Image? = null) : Scene() {
    lateinit var timer: Timer
    private val player: Player
    private val refreshDelay: Int = 8
    private val gameObjectList = ArrayList<AbstractGameObject>()
    private val rigidBodyList = ArrayList<RigidBody>()
    private val gameObjectsToRender = ArrayList<AbstractGameObject>()
    private var drawingDistance = 600
    private var useZonedDrawing = false

    init {
        // black background
        val windowDimension = MainFrame.windowDimension!!
        layers[9].addElement(
            if (backgroundImage == null)
                javadash.ui.Rectangle(
                    0,
                    0,
                    windowDimension.width,
                    windowDimension.height,
                    Color.BLACK
                )
            else
                ImageView(0, 0, windowDimension.width, windowDimension.height, backgroundImage)
        )

        // player
        player = Player(Vector(100, 400), Dimension(34, 34))
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
        if (useZonedDrawing) {
            gameObjectsToRender.reversed().forEach {
                it.paint(g2d)
            }
        } else {
            paintWithoutBackground(g2d)
        }
    }

    private fun rebuildIndex() {
        synchronized(gameObjectList) {
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

    private fun rebuildToRenderIndex() {
        synchronized(gameObjectsToRender) {
            gameObjectsToRender.clear()
        }
        for (i in 0..8) {
            layers[i].elements.reversed().forEach {
                if (it is AbstractGameObject) {
                    if (it is Rectangle) {
                        if (player.pos.x <= it.pos.x + it.width + 250 && it.pos.x - player.pos.x <= drawingDistance) {
                            synchronized(gameObjectsToRender) {
                                gameObjectsToRender.add(it)
                            }
                        }
                    } else {
                        if (it.pos.x + 300 > player.pos.x) {
                            synchronized(gameObjectsToRender) {
                                gameObjectsToRender.add(it)
                            }
                        }
                    }
                }
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
        val operation = Runnable {
            detectCollision()
            update()
        }
        timer = Timer(refreshDelay) {
            //val time = System.currentTimeMillis()
            val t = Thread(operation)
            t.start()
            t.join()
            //println(System.currentTimeMillis() - time)
        }
        timer.start()

        if (useZonedDrawing) {
            Timer(200) {
                Thread {
                    rebuildToRenderIndex()
                }.start()
            }.start()
        }
    }

    private fun update() {
        gameObjectList.forEach {
            it.update(refreshDelay)
        }
        player.update(refreshDelay)
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