import java.awt.Color
import scala.swing.Graphics2D

object Implicits {
  implicit class TupleOps[A: Numeric, B: Numeric](t: (A, B)) {
    import Numeric.Implicits._

    def +(other: (A, B)): (A, B) = (t._1 + other._1, t._2 + other._2)
    def *(other: (A, B)): (A, B) = (t._1 * other._1, t._2 * other._2)
  }

  implicit class Graphics2DOps(g: Graphics2D) {
    def translate(t: (Double, Double)): Unit = {
      g.translate(t._1, t._2)
    }
  }

  implicit class ColorOps(c: Color) {
    def withAlpha(a: Int): Color = {
      new Color(c.getRed, c.getGreen, c.getBlue, a)
    }
  }
}
