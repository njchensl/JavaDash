package javadash.game

import javadash.ui.Displayable
import javadash.ui.OptionPane
import javadash.util.Vector
import java.awt.*
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
class Player(pos: Vector, var dimension: Dimension = Dimension(34, 34)) : AbstractGameObject(pos) {
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
    killer: Boolean = false,
    var color: Color = Color.BLACK
) :
    AbstractGameObject(Vector(x, y), fixed, killer) {
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

class Square(x: Int, y: Int) : Rectangle(x, y, 34, 34, true, false), RigidBody {
    override fun paint(g2d: Graphics2D) {
        super.paint(g2d)
        // decoration

        val x = pos.x.toFloat()
        val y = pos.y.toFloat()

        // backup paint
        val originalPaint = g2d.paint

        // top
        g2d.paint = GradientPaint(x, y, Color.BLACK, x, y + 8, this.color)
        val top = Polygon()
        top.addPoint(x.toInt(), y.toInt())
        top.addPoint(x.toInt() + 10, y.toInt() + 10)
        top.addPoint(x.toInt() + width - 10, y.toInt() + 10)
        top.addPoint(x.toInt() + width, y.toInt())
        top.addPoint(x.toInt(), y.toInt())
        g2d.fill(top)

        // left
        g2d.paint = GradientPaint(x, y, Color.BLACK, x + 8, y, this.color)
        val left = Polygon()
        left.addPoint(x.toInt(), y.toInt())
        left.addPoint(x.toInt() + 10, y.toInt() + 10)
        left.addPoint(x.toInt() + 10, y.toInt() + height - 10)
        left.addPoint(x.toInt(), y.toInt() + height)
        left.addPoint(x.toInt(), y.toInt())
        g2d.fill(left)

        // bottom
        g2d.paint = GradientPaint(x, y + height, Color.BLACK, x, y + height - 8, this.color)
        val bottom = Polygon()
        bottom.addPoint(x.toInt(), (y + height).toInt())
        bottom.addPoint((x + width).toInt(), (y + height).toInt())
        bottom.addPoint((x + width - 10).toInt(), (y + height - 10).toInt())
        bottom.addPoint((x + 10).toInt(), (y + height - 10).toInt())
        bottom.addPoint(x.toInt(), (y + height).toInt())
        g2d.fill(bottom)

        // right
        g2d.paint = GradientPaint(x + width, y, Color.BLACK, x + width - 10, y, this.color)
        val right = Polygon()
        right.addPoint((x + width).toInt(), y.toInt())
        right.addPoint((x + width).toInt(), (y + height).toInt())
        right.addPoint((x + width - 10).toInt(), (y + height - 10).toInt())
        right.addPoint((x + width - 10).toInt(), (y + 10).toInt())
        right.addPoint((x + width).toInt(), y.toInt())
        g2d.fill(right)

        // restore paint
        g2d.paint = originalPaint

        // white border
        g2d.color = Color.WHITE
        g2d.drawRect(x.toInt(), y.toInt(), width, height)
    }

    override fun detectCollision(player: Player): CollisionEvent? {
        // TODO

        return null
    }
}