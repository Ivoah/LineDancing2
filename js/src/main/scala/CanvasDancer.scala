import Extensions.*
import CanvasExtensions.*

import org.scalajs.dom.CanvasRenderingContext2D

case class CanvasDancer(override val dance: Dance, override val starting_couple: Int, override val num_couples: Int, override val woman: Boolean, name: String) extends Dancer(dance, starting_couple, num_couples, woman) {
  private val WIDTH = 50
  private val HEIGHT = 25

  private val name_color = Color.BLACK
  private val body_color = if (woman) new Color(255, 105, 180) else new Color(0, 0, 255)
  private val head_color = body_color.darker

  private val name_color_sitting = Color.BLACK.withAlpha(64)
  private val body_color_sitting = body_color.withAlpha(64)
  private val head_color_sitting = head_color.withAlpha(64)

  def draw(count: Double, scale: (Double, Double))(implicit ctx: CanvasRenderingContext2D): Unit = {
    ctx.save()

    val pos = if (!sitting(count)) {
      dance.steps.filter(_._1.start < count%dance.length).flatMap { (range, steps) =>
        steps.map(_._2(this, count, math.min((count%dance.length - range.start)/range.length, 1)))
      }.flatten.foldLeft(starting_pos(count))(_ + _)
    } else starting_pos(count)

    ctx.translate(pos._1 * scale)
    ctx.rotate(pos._2)

    ctx.fillStyle = (if (sitting(count)) name_color_sitting else name_color).toString()
    ctx.fillText(name, -WIDTH/2, -HEIGHT/2)

    ctx.fillStyle = (if (sitting(count)) body_color_sitting else body_color).toString()
    ctx.fillOval(-WIDTH/2, -HEIGHT*3/10, WIDTH, HEIGHT*3/5)
    ctx.fillStyle = (if (sitting(count)) head_color_sitting else head_color).toString()
    ctx.fillOval(-HEIGHT/2, -HEIGHT/2, HEIGHT, HEIGHT)

    if (!sitting(count)) {
      ctx.fillStyle = Color.WHITE.toString()
      ctx.fillText((couple(count)%2 + 1).toString, -4, 5)
    }

    ctx.restore()
  }
}
