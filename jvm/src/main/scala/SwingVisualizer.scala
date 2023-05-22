import Extensions.*

import javax.sound.sampled.*
import javax.swing.Timer
import scala.swing.*
import scala.swing.event.*
import scala.util.Random
import java.nio.file.{Path, Files}
import java.awt.Font
import java.io.File

class SwingVisualizer(val yamlFile: Path, val num_couples: Int = 6) extends BorderPanel {
  peer.getFontMetrics(peer.getFont) // Load font into memory to avoid hang on first drawString call
  font = Font.createFont(Font.TRUETYPE_FONT, new File("Eczar.ttf")).deriveFont(13.0f)

  val dance = Dance.fromYaml(Files.readString(yamlFile))
  val visualizer = Visualizer(dance, num_couples)

  private val inputStream = AudioSystem.getAudioInputStream(yamlFile.getParent.resolve(dance.song).toFile)
  val song: Clip = AudioSystem.getLine(new DataLine.Info(classOf[Clip], inputStream.getFormat)).asInstanceOf[Clip]
  song.open(inputStream)

  private val timer: Timer = new Timer(10, _ => {
    repaint()
    progress.value = (song.getMicrosecondPosition/1000).toInt
  })

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    implicit val ctx = SwingDrawingContext(g)
    visualizer.draw(song.getMicrosecondPosition/1000)
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
