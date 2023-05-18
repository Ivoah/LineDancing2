import java.awt.Color
import scala.swing.Graphics2D

object SwingExtensions {
  extension (g: Graphics2D) {
    def translate(t: (Double, Double)): Unit = {
      g.translate(t._1, t._2)
    }
  }

  extension (c: Color) {
    def withAlpha(a: Int): Color = {
      new Color(c.getRed, c.getGreen, c.getBlue, a)
    }
  }
}
