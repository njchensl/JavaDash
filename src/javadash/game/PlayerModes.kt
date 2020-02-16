package javadash.game

import javadash.util.Vector

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
}

class DefaultPlayerMode : PlayerMode {
    /**
     * reserved for top collisions
     */
    var sliding: Boolean = false

    override fun update(player: Player, timeElapsed: Int) {
        if (sliding) {
            player.vel = Vector(player.vel.x, 0)
        } else {
            val accWeighed = player.acc / 1000 * timeElapsed
            player.vel += accWeighed
        }
        player.pos += player.vel / 1000 * timeElapsed

        // TODO finished? idk

        sliding = false
    }

    override fun resolveCollision(collisionEvent: CollisionEvent) {
        TODO("Not yet implemented")
    }
}