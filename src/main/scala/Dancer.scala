import java.awt.Color
import scala.swing.Graphics2D

import Implicits._

case class Dancer(var couple: Int, woman: Boolean, name: String) {

  val WIDTH = 50
  val HEIGHT = 25

  private val body_color = if (woman) new Color(255, 105, 180) else new Color(0, 0, 255)
  private val head_color = body_color.darker

  private val body_color_sitting = body_color.withAlpha(127)
  private val head_color_sitting = head_color.withAlpha(127)

  private var pos: ((Double, Double), Double) = ((couple, if (woman) 0 else 1), if (woman) 0 else math.Pi)
  private var step: Steps.Step = Steps.emptyStep

  var sitting: Boolean = false

  def change_step(new_step: Steps.Step): Unit = {
    step(this, 1).foreach(end => pos = (pos._1 + end._1, pos._2 + end._2))
    step = new_step
  }

  def draw(scale: (Double, Double), progress: Double)(implicit g: Graphics2D): Unit = {
    val transform = g.getTransform
    val paint = g.getPaint

    step(this, progress) match {
      case Some(step) =>
        g.translate((pos._1 + step._1) * scale)
        g.rotate(pos._2 + step._2)
      case None =>
        g.translate(pos._1 * scale)
        g.rotate(pos._2)
    }

    g.drawString(name, -WIDTH/2, -HEIGHT/2)
//    g.drawRect(-WIDTH/2, -HEIGHT/2, WIDTH, HEIGHT)

    g.setPaint(if (sitting) body_color_sitting else body_color)
    g.fillOval(-WIDTH/2, -HEIGHT*3/10, WIDTH, HEIGHT*3/5)
    g.setPaint(if (sitting) head_color_sitting else head_color)
    g.fillOval(-HEIGHT/2, -HEIGHT/2, HEIGHT, HEIGHT)

    g.setPaint(Color.WHITE)
    g.drawString(couple.toString, -4, 5)

    g.setTransform(transform)
    g.setPaint(paint)

  }
}
