package javadash.game

/**
 * ways of moving the player around in the scene
 */
interface PlayerMode {
    fun update(player: Player)
}

class DefaultPlayerMode : PlayerMode {
    override fun update(player: Player) {
        TODO("Not yet implemented")
    }
}