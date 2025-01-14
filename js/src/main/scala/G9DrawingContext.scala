import scala.scalajs.js.Dynamic
import org.scalajs.dom.document
import org.scalajs.dom.SVGTextElement

class G9DrawingContext(ctx: Dynamic) extends AbstractDrawingContext {
  override val width: Int = ctx.width.asInstanceOf[Int]
  override val height: Int = ctx.height.asInstanceOf[Int]

  var fill = Color.BLACK
  var transform = (-width.toDouble/2, -height.toDouble/2)

  override def withColor(color: Color)(thunk: => Unit): Unit = {
    val oldFill = fill
    fill = color
    thunk
    fill = oldFill
  }

  override def withTranslation(pos: (Double, Double))(thunk: => Unit): Unit = {
    val oldTransform = transform
    transform = pos
    thunk
    transform = oldTransform
  }

  override def withRotation(angle: Double)(thunk: => Unit): Unit = {
    thunk
  }

  override def drawString(str: String, x: Int, y: Int): Unit = {
    ctx.text(str, x + transform._1, y + transform._2, Dynamic.literal("fill" -> fill.toString, "font-family" -> "Eczar", "font-size" -> 13))
  }

  override def drawRectangle(x: Int, y: Int, w: Int, h: Int): Unit = {
    ctx.rect(x + transform._1, y + transform._2, w, h, Dynamic.literal("fill" -> fill.toString))
  }

  override def drawOval(x: Int, y: Int, w: Int, h: Int): Unit = {
    ctx.point(x + transform._1, y + transform._2, Dynamic.literal("r" -> w, "fill" -> fill.toString))
  }

  override def stringWidth(str: String): Int = {
    val svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
    svg.innerHTML = s"""<text font-family="Eczar" font-size="13px">$str</text>"""
    document.body.appendChild(svg)
    val width = svg.children.head.asInstanceOf[SVGTextElement].getComputedTextLength().toInt
    document.body.removeChild(svg)
    width
  }
}
