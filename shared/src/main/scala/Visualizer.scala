import Extensions.*

import scala.util.Random

case class Visualizer(dance: Dance, num_couples: Int = 6) {
  private val SCALE: (Double, Double) = (100, 150)
  private def ROOT(implicit ctx: AbstractDrawingContext): (Double, Double) = (
    (ctx.width - (num_couples - 1)*SCALE._1)/2,
    (ctx.height - (2 - 1)*SCALE._2)/2
  )

  private val dancers = {
    val women = Random.shuffle(Seq("Allison", "Cat", "Dana", "Emma", "Evelyn", "Geneva", "Hannah", "Heather", "Isa", "Janna", "Katie", "Lilly", "Lydia", "Maerin", "Sara", "Sarah", "Shai"))
    val men = Random.shuffle(Seq("Aaron", "Ben", "Charles", "Haddon", "Jacob G.", "Jacob H.", "John D.", "John K.", "Josh", "Micah", "Noobscout", "Tim D.", "Tim M."))
    (0 until num_couples).flatMap(c => Seq(
      Dancer(dance, c, num_couples, woman = true, women(c)),
      Dancer(dance, c, num_couples, woman = false, men(c))
    ))
  }

  def draw(ms: Double)(implicit ctx: AbstractDrawingContext): Unit = {
    val count = dance.ms_to_count(ms)
    val range = dance.range_at(count).getOrElse(0 until 0)
    val progress = (count%dance.length - range.start)/range.length

    ctx.clear()

    ctx.save()
    ctx.translate(ROOT)
    dancers.foreach(_.draw(count, SCALE))
    ctx.restore()

    dance.steps.get(range).foreach(_.zipWithIndex.foreach { case (step, i) =>
      ctx.drawString(f"${step._1} (${range.length} count${if (range.length == 1) "" else "s"}): ${progress*100}%.2f%%", 3, 20*(i + 1))
    })

    ctx.drawString(f"${dance.ms_to_count(ms)}%.2fc", 3, ctx.height - 30)
    ctx.drawString(s"${ms.toInt}ms", 3, ctx.height - 17)
  }
}
