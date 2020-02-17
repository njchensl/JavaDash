package javadash.game

import javadash.Main
import javadash.ui.Displayable
import javadash.ui.Scene
import javadash.util.Vector
import java.awt.Color
import java.awt.Dimension
import javax.swing.Timer

// TODO give the programmer a way to add objects to the class outside the class (external api)
//  (while adding things, make sure that the index is also updated!!!)
class GameScene : Scene() {
    private lateinit var timer: Timer
    private val player: Player
    private val refreshDelay: Int = 17
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