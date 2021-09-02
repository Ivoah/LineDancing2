import scala.swing._
import scala.swing.event._
import scala.util.Random
import javax.swing.Timer

import Implicits._

class Visualizer(val dance: Dance) extends BorderPanel {
  peer.getFontMetrics(peer.getFont) // Load font into memory to avoid hang on first drawString call

  private val NUM_COUPLES = 8
  private val SCALE: (Double, Double) = (100, 150)
  private def ROOT: (Double, Double) = (
    (size.width - (NUM_COUPLES - 1)*SCALE._1)/2,
    (size.height - (2 - 1)*SCALE._2)/2
  )

  private val dancers = {
    val women = Random.shuffle(Seq("Allison", "Cat", "Dana", "Emma", "Evelyn", "Geneva", "Hannah", "Heather", "Isa", "Janna", "Katie", "Lilly", "Lydia", "Maerin", "Sara", "Sarah", "Shai"))
    val men = Random.shuffle(Seq("Aaron", "Ben", "Charles", "Haddon", "Jacob G.", "Jacob H.", "John D.", "John K.", "Josh", "Micah", "Noobscout", "Tim D.", "Tim M."))
    (0 until NUM_COUPLES).flatMap(c => Seq(
      Dancer(c, woman = true, women(c)),
      Dancer(c, woman = false, men(c))
    ))
  }

  val timer: Timer = {
    var last_range = 0 until 0
    new Timer(10, _ => {
      val count = dance.ms_to_count(dance.song.getMicrosecondPosition/1000)
      val range = dance.range_at(count).getOrElse(0 until 0)
      val steps = dance.steps.getOrElse(range, Seq())
      if (range != last_range) {
        dancers.foreach { dancer =>
          dancer.change_step(steps.find(_._2(dancer, 0).nonEmpty).map(_._2).getOrElse(Steps.emptyStep))
        }
        last_range = range
      }
      repaint()
      progress.value = (dance.song.getMicrosecondPosition/1000).toInt
    })
  }

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    implicit val implicitGraphics: Graphics2D = g

    val count = dance.ms_to_count(dance.song.getMicrosecondPosition/1000)
    val range = dance.range_at(count).getOrElse(0 until 0)
    val progress = (count - range.start)/range.length

    val transform = g.getTransform
    g.translate(ROOT)
    dancers.foreach(_.draw(SCALE, progress))
    g.setTransform(transform)

    dance.steps.get(range).foreach(_.zipWithIndex.foreach { case (step, i) =>
      g.drawString(f"${step._1}: ${progress*100}%.2f%%", 3, 20*(i + 1))
    })
  }

  private val progress = new ProgressBar {
    min = 0
    max = (dance.song.getMicrosecondLength/1000).toInt
  }

  def play(): Unit = {
    dance.song.start()
    timer.start()
  }

  keys.reactions += {
    case KeyPressed(source, key, modifiers, location) if key == Key.Space =>
      if (dance.song.isRunning) {
        dance.song.stop()
        timer.stop()
      } else {
        dance.song.start()
        timer.start()
      }
    case KeyPressed(source, key, modifiers, location) if key == Key.Left =>
      val count = dance.ms_to_count(dance.song.getMicrosecondPosition/1000).toInt
      dance.song.setMicrosecondPosition(dance.count_to_ms(count - 1)*1000)
    case KeyPressed(source, key, modifiers, location) if key == Key.Right =>
      val count = dance.ms_to_count(dance.song.getMicrosecondPosition/1000).toInt
      dance.song.setMicrosecondPosition(dance.count_to_ms(count + 1)*1000)
  }
  listenTo(keys)

  layout(progress) = BorderPanel.Position.South

  focusable = true
  preferredSize = new Dimension(640, 480)
}
