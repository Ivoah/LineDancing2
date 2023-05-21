import org.scalajs.dom.CanvasRenderingContext2D

object CanvasExtensions {
  extension (ctx: CanvasRenderingContext2D) {
    def translate(t: (Double, Double)): Unit = {
      ctx.translate(t._1, t._2)
    }

    def fillOval(x: Int, y: Int, w: Int, h: Int): Unit = {
      ctx.beginPath();
      ctx.ellipse(x + w/2, y + h/2, w/2, h/2, 0, 0, 2 * math.Pi);
      ctx.fill();
    }
  }
}
