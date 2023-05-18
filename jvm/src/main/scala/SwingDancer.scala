import Extensions.*
import SwingExtensions.*

import java.awt.Color
import scala.swing.Graphics2D

case class SwingDancer(override val dance: Dance, override val starting_couple: Int, override val num_couples: Int, override val woman: Boolean, name: String) extends Dancer(dance, starting_couple, num_couples, woman) {
  private val WIDTH = 50
  private val HEIGHT = 25

  private val name_color = Color.BLACK
  private val body_color = if (woman) new Color(255, 105, 180) else new Color(0, 0, 255)
  private val head_color = body_color.darker

  private val name_color_sitting = Color.BLACK.withAlpha(64)
  private val body_color_sitting = body_color.withAlpha(64)
  private val head_color_sitting = head_color.withAlpha(64)

  def draw(count: Double, scale: (Double, Double))(implicit g: Graphics2D): Unit = {
    val transform = g.getTransform
    val paint = g.getPaint

    val pos = if (!sitting(count)) {
      dance.steps.filter(_._1.start < count%dance.length).flatMap { (range, steps) =>
        steps.map(_._2(this, count, math.min((count%dance.length - range.start)/range.length, 1)))
      }.flatten.foldLeft(starting_pos(count))(_ + _)
    } else starting_pos(count)

    g.translate(pos._1 * scale)
    g.rotate(pos._2)

    g.setPaint(if (sitting(count)) name_color_sitting else name_color)
    g.drawString(name, -WIDTH/2, -HEIGHT/2)

    g.setPaint(if (sitting(count)) body_color_sitting else body_color)
    g.fillOval(-WIDTH/2, -HEIGHT*3/10, WIDTH, HEIGHT*3/5)
    g.setPaint(if (sitting(count)) head_color_sitting else head_color)
    g.fillOval(-HEIGHT/2, -HEIGHT/2, HEIGHT, HEIGHT)

    if (!sitting(count)) {
      g.setPaint(Color.WHITE)
      g.drawString((couple(count)%2 + 1).toString, -4, 5)
    }

    g.setTransform(transform)
    g.setPaint(paint)
  }
}
