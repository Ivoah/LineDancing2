import Extensions.*
import CanvasExtensions.*

import org.scalajs.dom.*
import org.scalajs.dom.html.Canvas

import scala.scalajs.js.timers.*

import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@main
def main(): Unit = {
  val num_couples = 6
  val canvas = document.querySelector("canvas").asInstanceOf[Canvas]
  implicit val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
  ctx.font = "13px Eczar"

  val SCALE: (Double, Double) = (100, 150)
  val ROOT: (Double, Double) = (
    (canvas.width - (num_couples - 1)*SCALE._1)/2,
    (canvas.height - (2 - 1)*SCALE._2)/2
  )

  val dance = Dance.fromYaml("""song: Hole in the Wall.wav
marks: [1600, 33400, 66000, 98640, 131360, 162800, 194600, 226000]

steps:
  - 1st couple cast down 2 (4 counts)
  - 1st couple lead up 2 (8 counts)
  - 2nd couple cast up 2 (4 counts)
  - 2nd couple lead down 2 (8 counts)
  - 1st corners cross right shoulders (6 counts)
  - 2nd corners cross right shoulders (6 counts)
  - Circle left halfway (6 counts)
  - 1st couple cast down 1 while 2nd couple lead up 1 (6 counts)
""")

  val dancers = {
    val women = Random.shuffle(Seq("Allison", "Cat", "Dana", "Emma", "Evelyn", "Geneva", "Hannah", "Heather", "Isa", "Janna", "Katie", "Lilly", "Lydia", "Maerin", "Sara", "Sarah", "Shai"))
    val men = Random.shuffle(Seq("Aaron", "Ben", "Charles", "Haddon", "Jacob G.", "Jacob H.", "John D.", "John K.", "Josh", "Micah", "Noobscout", "Tim D.", "Tim M."))
    (0 until num_couples).flatMap(c => Seq(
      CanvasDancer(dance, c, num_couples, woman = true, women(c)),
      CanvasDancer(dance, c, num_couples, woman = false, men(c))
    ))
  }

  val audioElement = document.querySelector("audio").asInstanceOf[Audio]
  setInterval(10) {
    val count = dance.ms_to_count(audioElement.currentTime*1000)
    val range = dance.range_at(count).getOrElse(0 until 0)
    val progress = (count%dance.length - range.start)/range.length

    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.save()
    ctx.translate(ROOT)
    dancers.foreach(_.draw(count, SCALE))
    ctx.restore()

    dance.steps.get(range).foreach(_.zipWithIndex.foreach { case (step, i) =>
      ctx.fillText(f"${step._1} (${range.length} count${if (range.length == 1) "" else "s"}): ${progress*100}%.2f%%", 3, 20*(i + 1))
    })

    ctx.fillText(f"${dance.ms_to_count(audioElement.currentTime*1000)}%.2fc", 3, canvas.height - 30)
    ctx.fillText(s"${(audioElement.currentTime*1000).toInt}ms", 3, canvas.height - 17)
  }

  canvas.onclick = { _ =>
    if (audioElement.paused) audioElement.play()
    else audioElement.pause()
    audioElement.focus(new FocusOptions{focusVisible = false})
  }
}
