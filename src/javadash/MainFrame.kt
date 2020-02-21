package javadash

import javadash.game.*
import javadash.ui.*
import javadash.ui.Rectangle
import org.w3c.dom.Element
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import javax.swing.JFrame
import javax.swing.Timer
import javax.swing.WindowConstants
import javax.tools.ToolProvider
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.absoluteValue
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.system.exitProcess


class MainFrame : JFrame() {
    val windowDimension: Dimension
    private val _scenes = Stack<Scene>()
    private val timer: Timer
    var activeScene: Scene
        get() {
            return _scenes.peek()
        }
        set(value) {
            if (_scenes.size >= 1) {
                val scene = _scenes.peek()
                if (scene is GameScene) {
                    scene.timer.stop()
                }
                if (scene != value) {
                    _scenes.push(value)
                }
            } else {
                _scenes.push(value)
            }
        }
    var previousMouseLocation: Point = MouseInfo.getPointerInfo().location
    private val bf: BufferedImage
    private val canvas: Canvas
    private var paintTime = 100L

    override fun repaint() {
        throw IllegalStateException()
    }

    override fun update(g: Graphics?) {
        throw IllegalStateException()
    }

    override fun paint(g: Graphics) {
        paint(g as Graphics2D)
    }

    fun popScene() {
        if (_scenes.size > 1) {
            _scenes.pop()
        }
    }

    private fun paint(g: Graphics2D) {
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        activeScene.paint(g)
        previousMouseLocation = getMouseLocation()
    }

    init {
        val blankScene = Scene()
        blankScene.addElement(9, Rectangle(0, 0, Int.MAX_VALUE, Int.MAX_VALUE, Color.BLACK))
        activeScene = blankScene
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent) {}
            override fun windowClosing(e: WindowEvent) {
                if (activeScene !is OkCancelScene) {
                    OptionPane.showOkCancelDialog(
                        Runnable { exitProcess(0) },
                        message = "Are you sure you want to exit?"
                    )
                }
            }

            override fun windowClosed(e: WindowEvent) {}
            override fun windowIconified(e: WindowEvent) {}
            override fun windowDeiconified(e: WindowEvent) {}
            override fun windowActivated(e: WindowEvent) {}
            override fun windowDeactivated(e: WindowEvent) {}
        })
        this.extendedState = Frame.MAXIMIZED_BOTH
        this.isUndecorated = true
        canvas = Canvas()
        this.add(canvas)
        this.isVisible = true
        canvas.isFocusable = true
        this.windowDimension = Dimension(width, height)
        canvas.preferredSize = windowDimension
        canvas.createBufferStrategy(2)
        this.pack()
        this.bf = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val canvasBufferStrategy = canvas.bufferStrategy
        val timer = ActionListener {
            val time = System.currentTimeMillis()
            paint(canvasBufferStrategy.drawGraphics)
            canvasBufferStrategy.show()
            paintTime = System.currentTimeMillis() - time
        }
        this.timer = Timer(5, timer)
        this.timer.start()
        // drawing performance monitoring thread
        Thread {
            while (true) {
                Thread.sleep(100)
                val framerateExpected = 1000 / (paintTime + 0.000000001)
                val framerateActual = 1000 / this.timer.delay
                if (framerateExpected < framerateActual - 20) {
                    this.timer.delay++
                } else if (framerateExpected > framerateActual + 50 && this.timer.delay > 3) {
                    this.timer.delay--
                }
                //println("Framerate: " + 1000 / this.timer.delay)
            }
        }.start()
        canvas.addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent?) {
                activeScene.mouseReleased(e)
            }

            override fun mouseEntered(e: MouseEvent?) {}
            override fun mouseClicked(e: MouseEvent?) {
                // transfer this interrupt downstream
                activeScene.mouseClicked(e)
            }

            override fun mouseExited(e: MouseEvent?) {}
            override fun mousePressed(e: MouseEvent?) {
                activeScene.mousePressed(e)
            }
        })
        canvas.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
                activeScene.keyTyped(e)
            }

            override fun keyPressed(e: KeyEvent?) {
                activeScene.keyPressed(e)
            }

            override fun keyReleased(e: KeyEvent?) {
                activeScene.keyReleased(e)
            }
        })
        /*
        // test
        //this.activeScene.addElement(1, RectangularElement(0, 0, 100, 100, Color.RED))
        //this.activeScene.addElement(0, RectangularElement(25, 25, 50, 50, Color.BLACK))
        activeScene.addElement(9, Rectangle(0, 0, windowDimension.width, windowDimension.height, Color.BLACK))
        val btn = Button(400, 500, 80, 25, "Hello")
        btn.addActionListener(Runnable {
            JOptionPane.showMessageDialog(null, "Hello World")
        })
        activeScene.addElement(0, btn)
         */

        Thread {
            Thread.sleep(1)
            /*
            activeScene = GameScene()
            var gs = GroundSegment(0, 500, 1000, 1000)
            activeScene.addElement(8, gs)

            gs = GroundSegment(1000, 400, 100, 1000, Color.GREEN)
            activeScene.addElement(8, gs)

            gs = GroundSegment(1100, 300, 1000, 1000, Color.BLUE)
            activeScene.addElement(8, gs)

            val cs = CeilingSegment(500, -30, 300, 400)
            activeScene.addElement(8, cs)

            for (i in 0..9) {
                val block = Square(400 + i * 34, 350)
                activeScene.addElement(8, block)
            }

             */
            activeScene = readScene(File("scene.xml"))

            (activeScene as GameScene).start()
        }.start()
    }

    private fun readScene(f: File): GameScene {
        val scene = GameScene()
        val factory = DocumentBuilderFactory.newInstance()
        val documentBuilder = factory.newDocumentBuilder()
        val document = documentBuilder.parse(f)
        val rootElement = document.documentElement
        val nodeList = rootElement.childNodes
        for (i in 0 until nodeList.length) {
            val childElement = nodeList.item(i)
            if (childElement.nodeName == "object") {
                try {
                    val element = childElement as Element
                    // class type
                    val type = element.getAttribute("type")
                    val layer = element.getAttribute("layer").toInt()
                    if (layer < 0 || layer > 9) {
                        OptionPane.showMessageDialog("Error", "Layer number $layer out of bound")
                        continue
                    }
                    val klass = try {
                        Class.forName("javadash.game.$type").kotlin
                    } catch (e: ClassNotFoundException) {
                        OptionPane.showMessageDialog("Error", "Unexpected class type: javadash.game.$type")
                        continue
                    }

                    // get common constructor arguments
                    val x = element.getByName("x").toInt()
                    val y = element.getByName("y").toInt()

                    val color: Color
                    color = try {
                        Color::class.java.getField(element.getByName("color")).get(null) as Color
                    } catch (ignored: Exception) {
                        Color.BLUE
                    }
                    val gameObject = instantiateObject(element, klass, x, y, color)
                    if (gameObject != null) {
                        scene.addElement(layer, gameObject)
                    }
                } catch (e: Exception) {
                    OptionPane.showThrowable(e)
                }
            } else if (childElement.nodeName == "array") {
                val element = childElement as Element
                // get the first object
                var obj: Element? = null
                val nodes = element.childNodes
                for (j in 0 until nodes.length) {
                    if (nodes.item(j).nodeName == "object") {
                        obj = nodes.item(j) as Element
                        break
                    }
                }
                val layer = obj!!.getAttribute("layer").toInt()
                if (layer < 0 || layer > 9) {
                    OptionPane.showMessageDialog("Error", "Layer number $layer out of bound")
                }
                val type = obj.getAttribute("type")
                // get common constructor arguments
                val x = obj.getByName("x").toInt()
                val y = obj.getByName("y").toInt()
                val klass = try {
                    Class.forName("javadash.game.$type").kotlin
                } catch (e: ClassNotFoundException) {
                    OptionPane.showMessageDialog("Error", "Unexpected class type: javadash.game.$type")
                    continue
                }

                val color = try {
                    Color::class.java.getField(obj.getByName("color")).get(null) as Color
                } catch (ignored: Exception) {
                    Color.BLUE
                }
                val gameObject = instantiateObject(obj, klass, x, y, color)
                // get array length
                val arrayLength = element.getAttribute("length").toInt()

                if (gameObject != null) {
                    scene.addElement(layer, gameObject)
                    // instantiate additional objects based on array length
                    if (gameObject is javadash.game.Rectangle) {
                        val width = gameObject.width
                        for (j in 1 until arrayLength) {
                            val o = instantiateObject(obj, klass, x + j * width, y, color)
                            scene.addElement(layer, o!!)
                        }
                    }
                }
            }
        }


        return scene
    }


    private fun instantiateObject(
        element: Element,
        klass: KClass<out Any>,
        x: Int,
        y: Int,
        color: Color
    ): AbstractGameObject? {
        val primCon = klass.primaryConstructor!!
        var scriptInstance = BiConsumer<AbstractGameObject, Int> { _, _ -> }
        // get and compile script if needed
        @Suppress("UNCHECKED_CAST")
        try {
            val className = ("S" + Random().nextLong().absoluteValue).trim()
            val script = element.getByName("script").trim()
            val code = """
            import java.util.function.BiConsumer;
            import javadash.game.AbstractGameObject;
            
            public class $className<T, U> implements BiConsumer<T, U> {
                
                @Override
                public void accept(T t, U u) {
                    int timeElapsed = (int) u;
                    AbstractGameObject object = (AbstractGameObject) t;
                    $script
                }
                
                public static BiConsumer<AbstractGameObject, Integer> getScript() {
                    return new $className<AbstractGameObject, Integer>();
                }
            }
        """.trimIndent()
            //println(code)
            // write code to file
            val tempFile = File("$className.java")
            tempFile.createNewFile()
            val fos = FileOutputStream(tempFile)
            fos.write(code.toByteArray())
            fos.flush()
            fos.close()

            // compile
            val javaCompiler = ToolProvider.getSystemJavaCompiler()!!
            javaCompiler.run(null, null, null, "$className.java")
            //println(0)
            tempFile.delete()
            //println("compiled")
            // load
            val compileTemp = File("$className.class").absoluteFile
            //println(compileTemp.parent)
            val url: URL = File(compileTemp.parent).toURI().toURL()
            //println(url)
            val urls: Array<URL> = arrayOf(url)
            val cl: ClassLoader = URLClassLoader(urls)
            val cls = cl.loadClass(className)
            scriptInstance = cls.getDeclaredMethod("getScript").invoke(null) as BiConsumer<AbstractGameObject, Int>
            compileTemp.delete()
        } catch (e: Exception) {
            if (e !is IllegalStateException) {
                e.printStackTrace()
            }
        }

        when (klass) {
            GroundSegment::class -> {
                // get additional constructor arguments
                val width = element.getByName("width").toInt()
                val height = element.getByName("height").toInt()
                // instantiate
                return primCon.call(
                    x,
                    y,
                    width,
                    height,
                    color,
                    scriptInstance
                ) as GroundSegment
            }
            CeilingSegment::class -> {
                // get additional constructor arguments
                val width = element.getByName("width").toInt()
                val height = element.getByName("height").toInt()
                // instantiate
                return primCon.call(
                    x,
                    y,
                    width,
                    height,
                    color,
                    scriptInstance
                ) as CeilingSegment
            }
            Square::class -> {
                // instantiate
                return primCon.call(x, y, color, scriptInstance) as Square
            }
            Triangle::class -> {
                // get additional constructor arguments
                val faceUp = element.getByName("up").toBoolean()
                return primCon.call(x, y, faceUp, color, scriptInstance) as Triangle
            }
            else -> {
                OptionPane.showMessageDialog("Error", "Unexpected class type: $klass")
                return null
            }
        }
    }

    private fun readDefaultScene() {

    }
}

/**
 * extension function for element get by name
 */
fun Element.getByName(s: String): String {
    return this.getElementsByTagName(s).item(0).textContent
}