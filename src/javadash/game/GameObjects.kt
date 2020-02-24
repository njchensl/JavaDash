package javadash.game

import javadash.Main
import javadash.ui.Displayable
import javadash.util.Vector
import java.awt.*
import java.awt.event.KeyEvent
import java.util.function.BiConsumer

/**
 * for all non player rigid body game objects
 */
interface RigidBody {
    fun detectCollision(player: Player): CollisionEvent?
}

/**
 * for all non-UI game objects
 */
abstract class AbstractGameObject(
    @Volatile var pos: Vector,
    var isFixed: Boolean = false,
    var isKiller: Boolean = false,
    var updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Displayable {
    @Volatile
    var vel = Vector.ZeroVector

    @Volatile
    var acc = Vector.ZeroVector

    var x: Double
        get() = pos.x
        set(@Suppress("UNUSED_PARAMETER") value) {
            throw IllegalAccessException()
        }
    var y: Double
        get() = pos.y
        set(@Suppress("UNUSED_PARAMETER") value) {
            throw IllegalAccessException()
        }

    abstract override fun paint(g2d: Graphics2D)
    open fun update(timeElapsed: Int) {
        updatingScript.accept(this, timeElapsed)
    }
}

/**
 * the player
 */
class Player(pos: Vector, val dimension: Dimension = Dimension(34, 34)) : AbstractGameObject(pos) {
    var playerMode: PlayerMode = DefaultPlayerMode()

    init {
        acc = Vector(0, 10000)
        vel = Vector(600, 0)
    }

    override fun paint(g2d: Graphics2D) {
        g2d.color = Color.RED
        g2d.fillRect(x.toInt(), y.toInt(), dimension.width, dimension.height)
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
        Main.MAIN_FRAME.resetGameScene()
        System.gc()
        //OptionPane.showMessageDialog("Failed")
    }
}

open class Rectangle(
    x: Int,
    y: Int,
    var width: Int,
    var height: Int,
    fixed: Boolean = true,
    killer: Boolean = false,
    val color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    AbstractGameObject(Vector(x, y), fixed, killer, updatingScript) {
    override fun paint(g2d: Graphics2D) {
        g2d.color = color
        g2d.fillRect(x.toInt(), y.toInt(), width, height)
    }
}

/**
 * segment of the floor, top / left collision detections only
 */
class GroundSegment(
    x: Int, y: Int, width: Int, height: Int, color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Rectangle(x, y, width, height, true, false, color, updatingScript), RigidBody {
    private val decorationShape = java.awt.Rectangle(x, y, width, if (height < 200) height else 200)
    private val decorationGradientPaint = GradientPaint(
        x.toFloat(),
        y.toFloat(), Color.BLACK, x.toFloat(), (if (height < 200) y + height else y + 200).toFloat(), this.color
    )

    override fun paint(g2d: Graphics2D) {
        super.paint(g2d)
        // some more decorations

        g2d.paint = decorationGradientPaint
        g2d.fill(decorationShape)

        g2d.color = Color.WHITE
        g2d.drawRect(x.toInt(), y.toInt(), width, height)
    }

    override fun detectCollision(player: Player): CollisionEvent? {
        val pos = player.pos
        val dimension = player.dimension
        // top
        if (pos.x + dimension.width >= x && pos.x <= x + this.width) {
            if (pos.y + dimension.height >= y && pos.y + dimension.height <= y + 25) {
                return CollisionEvent(player, CollisionSide.TOP, this)
            }
        }
        // left
        if (pos.y + dimension.height >= y && pos.y <= y + this.height) {
            if (pos.x + dimension.width >= x && pos.x <= x + this.width) {
                return CollisionEvent(player, CollisionSide.LEFT, this)
            }
        }

        return null
    }
}

/**
 * segment of the ceiling, bottom / left collision detections only
 */
class CeilingSegment(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Rectangle(x, y, width, height, true, false, color, updatingScript), RigidBody {
    override fun detectCollision(player: Player): CollisionEvent? {
        val pos = player.pos
        val dimension = player.dimension

        // bottom
        if (pos.x + dimension.width >= x && pos.x <= x + this.width) {
            if (pos.y <= y + this.height && pos.y >= y + height - 10) {
                return CollisionEvent(player, CollisionSide.BOTTOM, this)
            }
        }
        // left
        if (pos.y + dimension.height >= y && pos.y <= y + this.height) {
            if (pos.x + dimension.width >= x && pos.x <= x + this.width) {
                return CollisionEvent(player, CollisionSide.LEFT, this)
            }
        }
        return null
    }
}

class Square(
    x: Int,
    y: Int,
    color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Rectangle(x, y, 34, 34, true, false, color, updatingScript), RigidBody {
    private val top: Polygon = Polygon()
    private val left: Polygon = Polygon()
    private val bottom: Polygon = Polygon()
    private val right: Polygon = Polygon()

    init {
        top.addPoint(x, y)
        top.addPoint(x + 10, y + 10)
        top.addPoint(x + width - 10, y + 10)
        top.addPoint(x + width, y)
        top.addPoint(x, y)
        left.addPoint(x, y)
        left.addPoint(x + 10, y + 10)
        left.addPoint(x + 10, y + height - 10)
        left.addPoint(x, y + height)
        left.addPoint(x, y)
        bottom.addPoint(x, (y + height))
        bottom.addPoint((x + width), (y + height))
        bottom.addPoint((x + width - 10), (y + height - 10))
        bottom.addPoint((x + 10), (y + height - 10))
        bottom.addPoint(x, (y + height))
        right.addPoint((x + width), y)
        right.addPoint((x + width), (y + height))
        right.addPoint((x + width - 10), (y + height - 10))
        right.addPoint((x + width - 10), (y + 10))
        right.addPoint((x + width), y)
    }

    override fun paint(g2d: Graphics2D) {
        super.paint(g2d)
        // decoration

        val x = x.toFloat()
        val y = y.toFloat()

        // side note: I just realized that there is probably no need to back up the paint as every time a gradient is
        // used a new paint is set, plus the fact that it does not affect the color
        // backup paint
        //val originalPaint = g2d.paint

        // top
        g2d.paint = GradientPaint(x, y, Color.BLACK, x, y + 8, this.color)
        g2d.fill(top)

        // left
        g2d.paint = GradientPaint(x, y, Color.BLACK, x + 8, y, this.color)
        g2d.fill(left)

        // bottom
        g2d.paint = GradientPaint(x, y + height, Color.BLACK, x, y + height - 8, this.color)
        g2d.fill(bottom)

        // right
        g2d.paint = GradientPaint(x + width, y, Color.BLACK, x + width - 10, y, this.color)
        g2d.fill(right)

        // restore paint
        //g2d.paint = originalPaint

        // white border
        g2d.color = Color.WHITE
        g2d.drawRect(x.toInt(), y.toInt(), width, height)
    }

    override fun detectCollision(player: Player): CollisionEvent? {
        val pos = player.pos
        val dimension = player.dimension
        // top
        if (pos.x + dimension.width >= x && pos.x <= x + this.width) {
            if (pos.y + dimension.height >= y && pos.y + dimension.height <= y + 25) {
                return CollisionEvent(player, CollisionSide.TOP, this)
            }
        }
        // bottom
        if (pos.x + dimension.width >= x && pos.x <= x + this.width) {
            if (pos.y <= y + this.height && pos.y >= y + height - 20) {
                return CollisionEvent(player, CollisionSide.BOTTOM, this)
            }
        }
        // left
        if (pos.y + dimension.height >= y && pos.y <= y + this.height) {
            if (pos.x + dimension.width >= x && pos.x <= x + this.width) {
                return CollisionEvent(player, CollisionSide.LEFT, this)
            }
        }
        return null
    }
}

/**
 * for up facing triangles, its position is defined as the coordinates of its bottom left corner
 * for down facing triangles, its position is defined as the coordinates of its top left corner
 */
class Triangle(
    x: Int,
    y: Int,
    private val faceUp: Boolean = true,
    color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Rectangle(x, y, 34, 34, true, true, color, updatingScript), RigidBody {
    private val triangle: Polygon = Polygon()

    init {
        if (faceUp) {
            triangle.addPoint(x, y)
            triangle.addPoint(x + width, y)
            triangle.addPoint(x + width / 2, y - height)
            triangle.addPoint(x, y)
        } else {
            triangle.addPoint(x, y)
            triangle.addPoint(x + width, y)
            triangle.addPoint(x + width / 2, y + height)
            triangle.addPoint(x, y)
        }
    }

    override fun paint(g2d: Graphics2D) {
        g2d.paint = if (faceUp) GradientPaint(
            x.toFloat(), (y - height).toFloat(), Color.BLACK,
            x.toFloat(), y.toFloat(), this.color
        ) else GradientPaint(
            x.toFloat(), (y + height).toFloat(), Color.BLACK,
            x.toFloat(), y.toFloat(), this.color
        )
        g2d.fill(triangle)
        // white boundary
        g2d.color = Color.WHITE
        g2d.draw(triangle)
    }

    override fun detectCollision(player: Player): CollisionEvent? {
        val x = player.x.toInt()
        val y = player.y.toInt()
        val width = player.dimension.width
        val height = player.dimension.height
        if (triangle.intersects(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())) {
            return CollisionEvent(player, CollisionSide.LEFT, this)
        }
        return null
    }
}

class MutableGroundSegment(
    x: Int, y: Int, width: Int, height: Int, color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) :
    Rectangle(x, y, width, height, true, false, color, updatingScript), RigidBody {

    override fun paint(g2d: Graphics2D) {
        super.paint(g2d)
        // some more decorations
        g2d.paint = GradientPaint(
            x.toFloat(),
            y.toFloat(), Color.BLACK, x.toFloat(), (if (height < 200) y + height else y + 200).toFloat(), this.color
        )
        g2d.fill(java.awt.Rectangle(x.toInt(), y.toInt(), width, if (height < 200) height else 200))

        g2d.color = Color.WHITE
        g2d.drawRect(x.toInt(), y.toInt(), width, height)
    }

    override fun detectCollision(player: Player): CollisionEvent? {
        val pos = player.pos
        val dimension = player.dimension
        // top
        if (pos.x + dimension.width >= x && pos.x <= x + this.width) {
            if (pos.y + dimension.height >= y && pos.y + dimension.height <= y + 25) {
                return CollisionEvent(player, CollisionSide.TOP, this)
            }
        }
        // left
        if (pos.y + dimension.height >= y && pos.y <= y + this.height) {
            if (pos.x + dimension.width >= x && pos.x <= x + this.width) {
                return CollisionEvent(player, CollisionSide.LEFT, this)
            }
        }

        return null
    }
}

class GravityReverser(
    x: Int,
    y: Int,
    color: Color = Color.BLUE,
    updatingScript: BiConsumer<AbstractGameObject, Int> = BiConsumer { _, _ -> }
) : Rectangle(
    x,
    y,
    40,
    40,
    true,
    false,
    color,
    updatingScript
), RigidBody {
    var used = false
    private val rect = java.awt.Rectangle(x, y, width, height)

    override fun paint(g2d: Graphics2D) {
        if (!used) {
            g2d.color = color
            g2d.fill(rect)
            g2d.color = Color.WHITE
            g2d.draw(rect)
        }
    }

    override fun detectCollision(player: Player): CollisionEvent? {
        val playerRect = java.awt.Rectangle(player.x.toInt(), player.y.toInt(), 34, 34)
        if (!used && rect.intersects(playerRect)) {
            used = true
            if (player.playerMode is DefaultPlayerMode) {
                (player.playerMode as DefaultPlayerMode).sliding = false
            }

            player.pos = Vector(player.x, this.y)
            player.acc = Vector(player.acc.x, -player.acc.y)
        }
        return null
    }
}

// TODO other elements
// TODO movable rectangles with changing colors