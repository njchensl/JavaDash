package javadash.game

import javadash.util.Vector
import java.awt.event.KeyEvent

enum class CollisionSide(x: Int) {
    LEFT(0), TOP(1), RIGHT(2), BOTTOM(3)
}

data class CollisionEvent(val player: Player, val collisionSide: CollisionSide, val gameObject: AbstractGameObject)

/**
 * ways of moving the player around in the scene
 */
interface PlayerMode {
    fun update(player: Player, timeElapsed: Int)
    fun resolveCollision(collisionEvent: CollisionEvent)
    fun keyPressed(e: KeyEvent?)
    fun keyReleased(e: KeyEvent?)
    fun keyTyped(e: KeyEvent?)
}

class DefaultPlayerMode : PlayerMode {
    /**
     * reserved for top collisions
     */
    @Volatile
    var sliding: Boolean = false
    private var slidingHeight: Int = 0

    private var jump: Boolean = false

    override fun update(player: Player, timeElapsed: Int) {
        when {
            sliding -> {
                if (jump) {
                    player.vel = Vector(player.vel.x, -2000)
                } else {
                    player.vel = Vector(player.vel.x, 0)
                    player.pos = Vector(player.pos.x, slidingHeight - player.dimension.height) // sliding on the surface
                }
            }
            else -> {
                val accWeighed = player.acc / 1000 * timeElapsed
                player.vel += accWeighed
            }
        }
        player.pos += player.vel / 1000 * timeElapsed

        sliding = false
    }

    override fun resolveCollision(collisionEvent: CollisionEvent) {
        val player = collisionEvent.player
        val gameObject = collisionEvent.gameObject
        val collisionSide = collisionEvent.collisionSide
        if (gameObject.isKiller) {
            player.kill()
        }
        when (collisionSide) {
            CollisionSide.TOP -> {
                // sliding
                sliding = true
                slidingHeight = gameObject.pos.y.toInt()
            }
            CollisionSide.LEFT -> {
                player.kill()
            }
            CollisionSide.BOTTOM -> {
                player.vel = Vector(player.vel.x, -player.vel.y)
                if (gameObject is Rectangle) {
                    player.pos = Vector(player.pos.x, gameObject.pos.y + gameObject.height + 1)
                }
            }
            else -> throw IllegalStateException()
        }
    }

    override fun keyPressed(e: KeyEvent?) {
        if (e!!.keyCode == KeyEvent.VK_SPACE) {
            jump = true
        }
    }

    override fun keyReleased(e: KeyEvent?) {
        if (e!!.keyCode == KeyEvent.VK_SPACE) {
            jump = false
        }
    }

    override fun keyTyped(e: KeyEvent?) {
        //TODO("Not yet implemented")
    }
}