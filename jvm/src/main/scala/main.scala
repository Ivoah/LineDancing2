import org.rogach.scallop.*

import java.awt.{Font, RenderingHints}
import java.io.File
import java.nio.file.{Files, Path}
import javax.sound.sampled.*
import javax.swing.Timer
import javax.swing.filechooser.FileNameExtensionFilter
import scala.swing.*
import scala.swing.event.*

class LineDancing2 extends MainFrame {
  private val mainFrame: MainFrame = this

  private var num_couples: Int = 6
  private var visualizer: Visualizer = null
  private var song: Clip = null

  private val graphicsPanel = new Panel {
    font = Font.createFont(Font.TRUETYPE_FONT, new File("Eczar.ttf")).deriveFont(13.0f)

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      implicit val ctx: Graphics2DDrawingContext = Graphics2DDrawingContext(g)
      if (visualizer != null) {
        visualizer.draw(song.getMicrosecondPosition / 1000)
      }
    }

    mouse.clicks.reactions += {
      case MousePressed(source, point, modifiers, clicks, triggersPopup) =>
        if (song.isRunning) pause()
        else play()
    }
    listenTo(mouse.clicks)

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

    focusable = true
    preferredSize = new Dimension(640, 480)
  }

  private val progress = new ProgressBar {
    min = 0

    mouse.clicks.reactions += {
      case MousePressed(source, point, modifiers, clicks, triggersPopup) =>
        setMicrosecondPosition((point.x.toDouble / source.size.width * song.getMicrosecondLength).toLong)
    }
    mouse.moves.reactions += {
      case MouseDragged(source, point, modifiers) =>
        setMicrosecondPosition((point.x.toDouble / source.size.width * song.getMicrosecondLength).toLong)
    }
    listenTo(mouse.clicks)
    listenTo(mouse.moves)
  }

  menuBar = new MenuBar {
    contents ++= Seq(
      new Menu("File") {
        contents ++= Seq(
          new MenuItem(Action("Load dance") {
            val chooser = new FileChooser(new File("."))
            chooser.fileFilter = new FileNameExtensionFilter("YAML files", "yml", "yaml")
            if (chooser.showOpenDialog(this) == FileChooser.Result.Approve) {
              if (song != null) song.close()
              loadDance(chooser.selectedFile.toPath)
            }
          })
        )
      },
      new Menu("Options") {
        contents ++= Seq(
          new MenuItem(Action("Number of couples") {
            Dialog.showInput(mainFrame, "Number of couples", initial = num_couples.toString).foreach { str =>
              str.toIntOption match {
                case Some(new_num_couples) if new_num_couples > 1 => setNumCouples(new_num_couples)
                case _ => Dialog.showMessage(mainFrame, s""""$str" is not a valid option""", "Error", Dialog.Message.Error)
              }
            }
          })
        )
      },
      new MenuItem(Action("Play/Pause") {
        if (song.isRunning) pause() else play()
      })
    )
  }

  contents = new BorderPanel {
    layout(graphicsPanel) = BorderPanel.Position.Center
    layout(progress) = BorderPanel.Position.South
  }

  title = "Line Dancing"

  def loadDance(path: Path): Unit = {
    val dance = Dance.fromYaml(Files.readString(path))
    val new_visualizer = Visualizer(dance, num_couples)
    val inputStream = AudioSystem.getAudioInputStream(path.getParent.resolve(dance.song).toFile)
    val new_song: Clip = AudioSystem.getLine(new DataLine.Info(classOf[Clip], inputStream.getFormat)).asInstanceOf[Clip]
    new_song.open(inputStream)
    progress.max = (new_song.getMicrosecondLength / 1000).toInt
    visualizer = new_visualizer
    song = new_song
    graphicsPanel.repaint()
  }

  def setNumCouples(new_num_couples: Int): Unit = {
    num_couples = new_num_couples
    visualizer = Visualizer(visualizer.dance, num_couples)
    graphicsPanel.repaint()
  }

  def setMicrosecondPosition(µs: Long): Unit = {
    song.setMicrosecondPosition(µs)
    timer.getActionListeners.foreach(_.actionPerformed(null))
  }

  def play(): Unit = {
    song.start()
    timer.start()
  }

  def pause(): Unit = {
    song.stop()
    timer.stop()
  }

  private val timer: Timer = new Timer(10, _ => {
    graphicsPanel.repaint()
    progress.value = (song.getMicrosecondPosition / 1000).toInt
  })
}

@main
def main(args: String*): Unit = {
  class Conf(args: Seq[String]) extends ScallopConf(args) {
    val dance: ScallopOption[Path] = opt[Path]()
    val couples: ScallopOption[Int] = opt[Int](default = Some(6))
    val ms: ScallopOption[Int] = opt[Int](default = Some(0))
    val play: ScallopOption[Boolean] = toggle(default = Some(false))

    dependsOnAll(ms, List(dance))
    dependsOnAll(play, List(dance))

    validateOpt(couples) {
      case Some(count) if count > 1 => Right(())
      case _ => Left("Invalid number of couples")
    }

    verify()
  }

  val conf = new Conf(args)

  val mainFrame = LineDancing2()
  conf.dance.foreach{ dance =>
    mainFrame.loadDance(dance)
    conf.couples.foreach(mainFrame.setNumCouples)
    mainFrame.setMicrosecondPosition(conf.ms() * 1000)
    if (conf.play()) {
      mainFrame.play()
    }
  }
  mainFrame.visible = true
}
