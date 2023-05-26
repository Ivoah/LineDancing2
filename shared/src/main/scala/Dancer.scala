import Extensions.*

case class Dancer(dance: Dance, starting_couple: Int, num_couples: Int, woman: Boolean, name: String) {
  private val WIDTH = 50
  private val HEIGHT = 25

  private val name_color = Color.BLACK
  private val body_color = if (woman) new Color(255, 105, 180) else new Color(0, 0, 255)
  private val head_color = body_color.darker

  private val name_color_sitting = Color.BLACK.dimmed
  private val body_color_sitting = body_color.dimmed
  private val head_color_sitting = head_color.dimmed

  private def starting_pos(count: Double): ((Double, Double), Double) = {
    val loop = count.toInt/dance.length
    (
      (
        couple(count) + (if (loop%2 != 0) 1 else 0),
        if (woman) 0 else 1
      ),
      if (woman) 0 else math.Pi
    )
  }

  def couple(count: Double): Int = {
    val loop = count.toInt/dance.length
    val offset = if (starting_couple%2 == 0) {
      starting_couple
    } else {
      num_couples*2 - starting_couple - 1
    }

    lazy val f: PartialFunction[Int, Int] = {
      case n if n < num_couples => n
      case n => (num_couples - 1) - f(n - num_couples)
    }
    f(loop + offset) - loop%2
  }

  def sitting(count: Double): Boolean = {
    val loop = count.toInt/dance.length
    couple(count) match {
      case c if c < 0 => true
      case c if num_couples%2 == 0 && loop%2 != 0 && c == num_couples - 2 => true
      case c if num_couples%2 != 0 && loop%2 == 0 && c == num_couples - 1 => true
      case _ => false
    }
  }

  def draw(count: Double, scale: (Double, Double))(implicit ctx: AbstractDrawingContext): Unit = {
    val pos = if (!sitting(count)) {
      dance.steps.filter(_._1.start < count%dance.length).flatMap { (range, steps) =>
        steps.map(_._2(this, count, math.min((count%dance.length - range.start)/range.length, 1)))
      }.flatten.foldLeft(starting_pos(count))(_ + _)
    } else starting_pos(count)

    ctx.withTranslation(pos._1 * scale) {
      ctx.withRotation(pos._2) {
        ctx.withColor(if (sitting(count)) name_color_sitting else name_color) {
          ctx.drawString(name, -WIDTH/2, -HEIGHT/2)
        }
    
        ctx.withColor(if (sitting(count)) body_color_sitting else body_color) {
          ctx.drawOval(-WIDTH/2, -HEIGHT*3/10, WIDTH, HEIGHT*3/5)
        }
        ctx.withColor(if (sitting(count)) head_color_sitting else head_color) {
          ctx.drawOval(-HEIGHT/2, -HEIGHT/2, HEIGHT, HEIGHT)
        }
    
        if (!sitting(count)) {
          ctx.withColor(Color.WHITE) {
            ctx.drawString((couple(count)%2 + 1).toString, -4, 5)
          }
        }
      }
    }
  }
}
