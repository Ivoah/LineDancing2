import scala.swing._
import scala.swing.event._
import scala.util.Random
import javax.swing.Timer

import Extensions._

class Visualizer(val dance: Dance) extends BorderPanel {
  peer.getFontMetrics(peer.getFont) // Load font into memory to avoid hang on first drawString call

  private val NUM_COUPLES = 6
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
  if (NUM_COUPLES%2 == 1) dancers.filter(_.couple == NUM_COUPLES - 1).foreach(_.sitting = true)

  private val timer: Timer = {
    var last_range = 0 until 0
    new Timer(10, _ => {
      val count = dance.ms_to_count(dance.song.getMicrosecondPosition/1000)
      val range = dance.range_at(count).getOrElse(0 until 0)
      val steps = dance.steps.getOrElse(range, Seq())
      if (range != last_range) { // New move
        if (range.start < last_range.start) { // Start from top of dance
          if (dancers.exists(_.couple == -1)) { // Head couple sitting out
            for (dancer <- dancers) {
              if (dancer.sitting) {
                dancer.couple += 1
                dancer.sitting = false
              } else {
                if (dancer.couple%2 == 0) dancer.couple += 2
                if (dancer.couple == NUM_COUPLES - 1) dancer.sitting = true
              }
            }
          } else {
            for (dancer <- dancers) {
              if (dancer.sitting) {
                dancer.couple -= 1
                dancer.sitting = false
              } else {
                if (dancer.couple%2 != 0) dancer.couple -= 2
                if (dancer.couple == -1 || dancer.couple == NUM_COUPLES - 2) dancer.sitting = true
              }
            }
          }
        }
        for (dancer <- dancers) {
          dancer.change_step(
            if (dancer.sitting) Steps.emptyStep
            else steps.find(_._2(dancer, 0).nonEmpty).map(_._2).getOrElse(Steps.emptyStep)
          )
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
    val progress = (count%dance.length - range.start)/range.length

    val transform = g.getTransform
    g.translate(ROOT)
    dancers.foreach(_.draw(SCALE, progress))
    g.setTransform(transform)

    dance.steps.get(range).foreach(_.zipWithIndex.foreach { case (step, i) =>
      g.drawString(f"${step._1} (${range.length} count${if (range.length == 1) "" else "s"}): ${progress*100}%.2f%%", 3, 20*(i + 1))
    })

    g.drawString(f"${dance.ms_to_count(dance.song.getMicrosecondPosition/1000)}%.2fc", 3, size.height - 30)
    g.drawString(s"${dance.song.getMicrosecondPosition/1000}ms", 3, size.height - 17)
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
//    case KeyPressed(source, key, modifiers, location) if key == Key.Left =>
//      val count = dance.ms_to_count(dance.song.getMicrosecondPosition/1000).toInt
//      dance.song.setMicrosecondPosition(dance.count_to_ms(count - 1)*1000)
//    case KeyPressed(source, key, modifiers, location) if key == Key.Right =>
//      val count = dance.ms_to_count(dance.song.getMicrosecondPosition/1000).toInt
//      dance.song.setMicrosecondPosition(dance.count_to_ms(count + 1)*1000)
  }
  listenTo(keys)

  layout(progress) = BorderPanel.Position.South

  focusable = true
  preferredSize = new Dimension(640, 480)
}
