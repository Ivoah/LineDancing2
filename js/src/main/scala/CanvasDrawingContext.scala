import org.scalajs.dom.CanvasRenderingContext2D

case class CanvasDrawingContext(ctx: CanvasRenderingContext2D, scale: Double) extends AbstractDrawingContext {
  ctx.font = "13px Eczar"
  ctx.shadowOffsetX = 5
  ctx.shadowOffsetY = 5
  ctx.shadowBlur = 15

  val width: Int = (ctx.canvas.width/scale).toInt
  val height: Int = (ctx.canvas.height/scale).toInt

  def clear(): Unit = ctx.clearRect(0, 0, width, height)

  def withColor(color: Color)(thunk: => Unit): Unit = {
    ctx.save()
    ctx.fillStyle = color.toString()
    thunk
    ctx.restore()
  }

  def withShadow(thunk: => Unit): Unit = {
    ctx.shadowColor = "rgba(0, 0, 0, 0.5)"
    thunk
    ctx.shadowColor = ""
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
  def drawRectangle(x: Int, y: Int, w: Int, h: Int): Unit = ctx.fillRect(x, y, w, h)

  def stringWidth(str: String): Int = ctx.measureText(str).width.toInt
}
