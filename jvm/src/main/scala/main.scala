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
  private var visualizer: Option[Visualizer] = None
  private var song: Option[Clip] = None

  private val canvas = new Component {
    font = Font.createFont(Font.TRUETYPE_FONT, new File("Eczar.ttf")).deriveFont(13.0f)

    private val BACKGROUND = java.awt.Color(252, 245, 229)
    override def paintComponent(g: Graphics2D): Unit = {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g.setBackground(BACKGROUND)
      g.clearRect(0, 0, g.getClipBounds().width, g.getClipBounds().height)

      implicit val ctx: Graphics2DDrawingContext = Graphics2DDrawingContext(g)
      visualizer.foreach(_.draw(song.get.getMicrosecondPosition / 1000))
    }

    mouse.clicks.reactions += {
      case MousePressed(source, point, modifiers, clicks, triggersPopup) =>
        if (song.exists(_.isRunning)) pause()
        else play()
    }
    listenTo(mouse.clicks)

    keys.reactions += {
      case KeyPressed(source, key, modifiers, location) if key == Key.Space =>
        if (song.exists(_.isRunning)) pause()
        else play()
      case KeyPressed(source, key, modifiers, location) if key == Key.Left =>
        setMicrosecondPosition(song.map(_.getMicrosecondPosition - 1000000).getOrElse(0))
      case KeyPressed(source, key, modifiers, location) if key == Key.Right =>
        setMicrosecondPosition(song.map(_.getMicrosecondPosition + 1000000).getOrElse(0))
    }
    listenTo(keys)

    focusable = true
    preferredSize = new Dimension(640, 480)
  }

  private val progress = new ProgressBar {
    min = 0

    mouse.clicks.reactions += {
      case MousePressed(source, point, modifiers, clicks, triggersPopup) =>
        setMicrosecondPosition(song.map(s => (point.x.toDouble / source.size.width * s.getMicrosecondLength)).getOrElse(0.0).toLong)
    }
    mouse.moves.reactions += {
      case MouseDragged(source, point, modifiers) =>
        setMicrosecondPosition(song.map(s => (point.x.toDouble / source.size.width * s.getMicrosecondLength)).getOrElse(0.0).toLong)
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
              song.foreach(_.close())
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
        if (song.exists(_.isRunning)) pause() else play()
      })
    )
  }

  contents = new BorderPanel {
    layout(canvas) = BorderPanel.Position.Center
    layout(progress) = BorderPanel.Position.South
  }

  title = "Line Dancing"

  def loadDance(path: Path): Unit = {
    val dance = Dance.fromYaml(Files.readString(path))
    val inputStream = AudioSystem.getAudioInputStream(path.getParent.resolve(dance.song).toFile)
    visualizer = Some(Visualizer(dance, num_couples))
    song = Some(AudioSystem.getLine(new DataLine.Info(classOf[Clip], inputStream.getFormat)).asInstanceOf[Clip])
    song.get.open(inputStream)
    progress.max = (song.get.getMicrosecondLength / 1000).toInt
    canvas.repaint()
  }

  def setNumCouples(new_num_couples: Int): Unit = {
    num_couples = new_num_couples
    visualizer = visualizer.map(v => Visualizer(v.dance, num_couples))
    canvas.repaint()
  }

  def setMicrosecondPosition(µs: Long): Unit = {
    song.foreach(_.setMicrosecondPosition(µs))
    timer.getActionListeners.foreach(_.actionPerformed(null))
  }

  def play(): Unit = {
    song.foreach(_.start())
    timer.start()
  }

  def pause(): Unit = {
    song.foreach(_.stop())
    timer.stop()
  }

  private val timer: Timer = new Timer(10, _ => {
    canvas.repaint()
    progress.value = song.map(s => (s.getMicrosecondPosition / 1000).toInt).getOrElse(0)
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
  conf.dance.foreach(mainFrame.loadDance)
  conf.couples.foreach(mainFrame.setNumCouples)
  mainFrame.setMicrosecondPosition(conf.ms() * 1000)
  if (conf.play()) {
    mainFrame.play()
  }
  mainFrame.visible = true
}
