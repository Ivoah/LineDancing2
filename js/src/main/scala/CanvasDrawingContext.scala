import org.scalajs.dom.CanvasRenderingContext2D

case class CanvasDrawingContext(ctx: CanvasRenderingContext2D) extends AbstractDrawingContext {
  ctx.font = "13px Eczar"
  val width: Int = ctx.canvas.width
  val height: Int = ctx.canvas.height

  def clear(): Unit = ctx.clearRect(0, 0, width, height)

  def save(): Unit = ctx.save()
  def restore(): Unit = ctx.restore()

  def setColor(color: Color): Unit = ctx.fillStyle = color.toString()

  def translate(pos: (Double, Double)): Unit = ctx.translate(pos._1, pos._2)
  def rotate(angle: Double): Unit = ctx.rotate(angle)

  def drawString(str: String, x: Int, y: Int): Unit = ctx.fillText(str, x, y)
  def drawOval(x: Int, y: Int, w: Int, h: Int): Unit = {
      ctx.beginPath();
      ctx.ellipse(x + w/2, y + h/2, w/2, h/2, 0, 0, 2 * math.Pi);
      ctx.fill();
    }
}
