import java.awt.Graphics2D
import java.awt.Paint
import java.awt.{Color => AWTColor}

case class SwingDrawingContext(g: Graphics2D) extends AbstractDrawingContext {
  val width: Int = g.getClipBounds().width
  val height: Int = g.getClipBounds().height

  def withColor(color: Color)(thunk: => Unit): Unit = {
    val paint = g.getPaint()
    g.setPaint(new AWTColor(color.r, color.g, color.b, color.a))
    thunk
    g.setPaint(paint)
  }

  def withTranslation(pos: (Double, Double))(thunk: => Unit): Unit = {
    val transform = g.getTransform()
    g.translate(pos._1, pos._2)
    thunk
    g.setTransform(transform)
  }

  def withRotation(angle: Double)(thunk: => Unit): Unit = {
    val transform = g.getTransform()
    g.rotate(angle)
    thunk
    g.setTransform(transform)
  }

  def drawString(str: String, x: Int, y: Int): Unit = g.drawString(str, x, y)
  def drawRectangle(x: Int, y: Int, w: Int, h: Int) = g.fillRect(x, y, w, h)
  def drawOval(x: Int, y: Int, w: Int, h: Int): Unit = g.fillOval(x, y, w, h)

  def stringWidth(str: String): Int = g.getFontMetrics().stringWidth(str)
  // def stringHeight(str: String): Int = {
  //   val frc = g.getFontRenderContext()
  //   val gv = g.getFont().createGlyphVector(frc, str)
  //   gv.getVisualBounds().getHeight().toInt
  // }
}
