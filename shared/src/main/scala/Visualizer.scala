import Extensions.*

import scala.util.Random

case class Visualizer(dance: Dance, num_couples: Int = 6) {
  private val SCALE: (Double, Double) = (100, 150)
  private def ROOT(implicit ctx: AbstractDrawingContext): (Double, Double) = (
    (ctx.width - (num_couples - 1)*SCALE._1)/2,
    (ctx.height - (2 - 1)*SCALE._2)/2
  )

  private val dancers = {
    val women = Random.shuffle(Names.women)
    val men = Random.shuffle(Names.men)
    (0 until num_couples).flatMap(c => Seq(
      Dancer(dance, c, num_couples, woman = true, women(c)),
      Dancer(dance, c, num_couples, woman = false, men(c))
    ))
  }

  def draw(ms: Double)(implicit ctx: AbstractDrawingContext): Unit = {
    val count = dance.ms_to_count(ms)
    val range = dance.range_at(count).getOrElse(0 until 0)
    val progress = (count%dance.length - range.start)/range.length

    ctx.withTranslation(ROOT) {
      dancers.foreach(_.draw(count, SCALE))
    }

    val currentSteps = dance.steps(range).map(s => s"${s._1} (${range.length} count${if (range.length == 1) "" else "s"})")
    currentSteps.zipWithIndex.foreach { case (step, i) =>
      ctx.withColor(Color.GREEN) {
        ctx.drawRectangle(3, 5 + 20*i, (ctx.stringWidth(step)*progress).toInt, 20)
      }
      ctx.drawString(step, 3, 20*(i + 1))
    }

    ctx.withColor(Color.BLACK.dimmed) {
      dance.range_at(range.end + 1).foreach { nextRange =>
        val nextSteps = dance.steps(nextRange).map(s => s"${s._1} (${nextRange.length} count${if (nextRange.length == 1) "" else "s"})")
        nextSteps.zipWithIndex.foreach { case (step, i) =>
          ctx.drawString(step, 3, 20*(i + currentSteps.length + 1))
        }
      }
    }

    ctx.drawString(f"${dance.ms_to_count(ms)}%.2fc", 3, ctx.height - 15)
    ctx.drawString(s"${ms.toInt}ms", 3, ctx.height)
  }
}
