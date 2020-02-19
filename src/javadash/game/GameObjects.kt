package javadash.game

import javadash.ui.Displayable
import javadash.ui.OptionPane
import javadash.util.Vector
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.event.KeyEvent

/**
 * for all non player rigid body game objects
 */
interface RigidBody {
    fun detectCollision(player: Player): CollisionEvent?
}

/**
 * for all non-UI game objects
 */
abstract class AbstractGameObject(var pos: Vector, var isFixed: Boolean = false, var isKiller: Boolean = false) :
    Displayable {
    var vel = Vector.ZeroVector
    var acc = Vector.ZeroVector

    abstract override fun paint(g2d: Graphics2D)
    abstract fun update(timeElapsed: Int)
}

/**
 * the player
 */
class Player(pos: Vector, var dimension: Dimension = Dimension(20, 20)) : AbstractGameObject(pos) {
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
        OptionPane.showMessageDialog("Failed")
    }
}

open class Rectangle(
    x: Int,
    y: Int,
    var width: Int,
    var height: Int,
    fixed: Boolean = true,
    killer: Boolean = false
) :
    AbstractGameObject(Vector(x, y), fixed, killer) {
    var color: Color = Color.BLACK
    override fun paint(g2d: Graphics2D) {
        g2d.color = color
        g2d.fillRect(pos.x.toInt(), pos.y.toInt(), width, height)
    }

    override fun update(timeElapsed: Int) {
    }
}

/**
 * segment of the floor, top / left collision detections only
 */
class GroundSegment(x: Int, y: Int, width: Int, height: Int) : Rectangle(x, y, width, height, true, false), RigidBody {
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
class CeilingSegment(x: Int, y: Int, width: Int, height: Int) : Rectangle(x, y, width, height, true, false), RigidBody {
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

class Square(x: Int, y: Int) : Rectangle(x, y, 20, 20, true, false), RigidBody {
    override fun detectCollision(player: Player): CollisionEvent? {
        // TODO

        return null
    }
}