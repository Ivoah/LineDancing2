import java.awt.Graphics2D
import scala.collection.mutable.Stack
import java.awt.geom.AffineTransform
import java.awt.Paint
import java.awt.{Color => AWTColor}

case class SwingDrawingContext(g: Graphics2D) extends AbstractDrawingContext {
  val width: Int = g.getClipBounds().width
  val height: Int = g.getClipBounds().height

  def clear(): Unit = g.clearRect(0, 0, width, height)

  val transformStack = Stack[(AffineTransform, Paint)]()
  def save(): Unit = transformStack.push((g.getTransform(), g.getPaint()))
  def restore(): Unit = {
    val (transform, paint) = transformStack.pop()
    g.setTransform(transform)
    g.setPaint(paint)
  }

  def setColor(color: Color): Unit = g.setPaint(new AWTColor(color.r, color.g, color.b, color.a))

  def translate(pos: (Double, Double)): Unit = g.translate(pos._1, pos._2)
  def rotate(angle: Double): Unit = g.rotate(angle)

  def drawString(str: String, x: Int, y: Int): Unit = g.drawString(str, x, y)
  def drawOval(x: Int, y: Int, w: Int, h: Int): Unit = g.fillOval(x, y, w, h)
}
