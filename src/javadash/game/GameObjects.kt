package javadash.game

import javadash.ui.Displayable
import javadash.util.Vector
import java.awt.Graphics2D

/**
 * for all non player rigid body game objects
 */
interface RigidBody {
    fun detectCollision(player: Player): Boolean
}

/**
 * for all non-UI game objects
 */
abstract class AbstractGameObject(var pos: Vector, var fixed: Boolean = false, var killer: Boolean = false) : Displayable {
    var vel = Vector(0, 0)
    var acc = Vector(0, 0)

    abstract override fun paint(g2d: Graphics2D)
    abstract fun update(timeElapsed: Int)
}

/**
 * the player
 */
class Player(pos: Vector) : AbstractGameObject(pos) {
    var playerMode: PlayerMode = DefaultPlayerMode()

    override fun paint(g2d: Graphics2D) {
        TODO("Not yet implemented")
    }

    /**
     * update the player according to the game mode
     */
    override fun update(timeElapsed: Int) {
        playerMode.update(this)
    }
}