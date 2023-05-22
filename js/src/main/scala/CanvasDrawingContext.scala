import org.scalajs.dom.CanvasRenderingContext2D

case class CanvasDrawingContext(ctx: CanvasRenderingContext2D) extends AbstractDrawingContext {
  ctx.font = "13px Eczar"
  val width: Int = ctx.canvas.width
  val height: Int = ctx.canvas.height

  def clear(): Unit = ctx.clearRect(0, 0, width, height)

  def withColor(color: Color)(thunk: => Unit): Unit = {
    ctx.save()
    ctx.fillStyle = color.toString()
    thunk
    ctx.restore()
  }

  def withTranslation(pos: (Double, Double))(thunk: => Unit): Unit = {
    ctx.save()
    ctx.translate(pos._1, pos._2)
    thunk
    ctx.restore()
  }
  def withRotation(angle: Double)(thunk: => Unit): Unit = {
    ctx.save()
    ctx.rotate(angle)
    thunk
    ctx.restore()
  }

  def drawString(str: String, x: Int, y: Int): Unit = ctx.fillText(str, x, y)
  def drawOval(x: Int, y: Int, w: Int, h: Int): Unit = {
    ctx.beginPath();
    ctx.ellipse(x + w/2, y + h/2, w/2, h/2, 0, 0, 2 * math.Pi);
    ctx.fill();
  }
}
