import Extensions.*

import javax.sound.sampled.*
import javax.swing.Timer
import scala.swing.*
import scala.swing.event.*
import scala.util.Random

class Visualizer(val dance: Dance, val num_couples: Int = 6) extends BorderPanel {
  peer.getFontMetrics(peer.getFont) // Load font into memory to avoid hang on first drawString call

  private val inputStream = AudioSystem.getAudioInputStream(dance.song.toFile)
  val song: Clip = AudioSystem.getLine(new DataLine.Info(classOf[Clip], inputStream.getFormat)).asInstanceOf[Clip]
  song.open(inputStream)

  // val volume = song.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl]
  // volume.setValue(volume.getMinimum)

  private val SCALE: (Double, Double) = (100, 150)
  private def ROOT: (Double, Double) = (
    (size.width - (num_couples - 1)*SCALE._1)/2,
    (size.height - (2 - 1)*SCALE._2)/2
  )

  private val dancers = {
    val women = Random.shuffle(Seq("Allison", "Cat", "Dana", "Emma", "Evelyn", "Geneva", "Hannah", "Heather", "Isa", "Janna", "Katie", "Lilly", "Lydia", "Maerin", "Sara", "Sarah", "Shai"))
    val men = Random.shuffle(Seq("Aaron", "Ben", "Charles", "Haddon", "Jacob G.", "Jacob H.", "John D.", "John K.", "Josh", "Micah", "Noobscout", "Tim D.", "Tim M."))
    (0 until num_couples).flatMap(c => Seq(
      Dancer(dance, c, num_couples, woman = true, women(c)),
      Dancer(dance, c, num_couples, woman = false, men(c))
    ))
  }
//  if (num_couples%2 == 1) dancers.filter(_.couple == num_couples - 1).foreach(_.sitting = true)

  private val timer: Timer = new Timer(10, _ => {
    repaint()
    progress.value = (song.getMicrosecondPosition/1000).toInt
  })

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    implicit val implicitGraphics: Graphics2D = g

    val count = dance.ms_to_count(song.getMicrosecondPosition/1000)
    val range = dance.range_at(count).getOrElse(0 until 0)
    val progress = (count%dance.length - range.start)/range.length

    val transform = g.getTransform
    g.translate(ROOT)
    dancers.foreach(_.draw(count, SCALE))
    g.setTransform(transform)

    dance.steps.get(range).foreach(_.zipWithIndex.foreach { case (step, i) =>
      g.drawString(f"${step._1} (${range.length} count${if (range.length == 1) "" else "s"}): ${progress*100}%.2f%%", 3, 20*(i + 1))
    })

    g.drawString(f"${dance.ms_to_count(song.getMicrosecondPosition/1000)}%.2fc", 3, size.height - 30)
    g.drawString(s"${song.getMicrosecondPosition/1000}ms", 3, size.height - 17)
  }

  private val progress = new ProgressBar {
    min = 0
    max = (song.getMicrosecondLength/1000).toInt
    mouse.clicks.reactions += {
      case MousePressed(source, point, modifiers, clicks, triggersPopup) =>
        setMicrosecondPosition((point.x.toDouble/source.size.width*song.getMicrosecondLength).toLong)
    }
    mouse.moves.reactions += {
      case MouseDragged(source, point, modifiers) =>
        setMicrosecondPosition((point.x.toDouble/source.size.width*song.getMicrosecondLength).toLong)
    }
    listenTo(mouse.clicks)
    listenTo(mouse.moves)
  }

  def play(): Unit = {
    song.start()
    timer.start()
  }

  def pause(): Unit = {
    song.stop()
    timer.stop()
  }

  def close(): Unit = {
    song.close()
    timer.stop()
  }

  def setMicrosecondPosition(µs: Long): Unit = {
    song.setMicrosecondPosition(µs)
    timer.getActionListeners.foreach(_.actionPerformed(null))
  }

  keys.reactions += {
    case KeyPressed(source, key, modifiers, location) if key == Key.Space =>
      if (song.isRunning) pause()
      else play()
    case KeyPressed(source, key, modifiers, location) if key == Key.Left =>
      setMicrosecondPosition(song.getMicrosecondPosition - 1000000)
    case KeyPressed(source, key, modifiers, location) if key == Key.Right =>
      setMicrosecondPosition(song.getMicrosecondPosition + 1000000)
  }
  listenTo(keys)

  layout(progress) = BorderPanel.Position.South

  focusable = true
  preferredSize = new Dimension(640, 480)
}
