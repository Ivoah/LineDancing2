import java.io.File
import java.nio.file.Path
import javax.sound.sampled.*
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.Timer
import java.awt.Font
import scala.swing.*
import scala.swing.event.*
import java.nio.file.Files

@main
def main(): Unit = {
  val mainFrame = new MainFrame {
    private val mainFrame = this
    font = Font.createFont(Font.TRUETYPE_FONT, new File("Eczar.ttf")).deriveFont(13.0f)

    private var visualizer: Visualizer = null
    private var song: Clip = null

    private val graphicsPanel = new Panel {
      override def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)
        implicit val ctx = SwingDrawingContext(g)
        visualizer.draw(song.getMicrosecondPosition/1000)
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
          setMicrosecondPosition((point.x.toDouble/source.size.width*song.getMicrosecondLength).toLong)
      }
      mouse.moves.reactions += {
        case MouseDragged(source, point, modifiers) =>
          setMicrosecondPosition((point.x.toDouble/source.size.width*song.getMicrosecondLength).toLong)
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
                song.close()
                loadDance(chooser.selectedFile.toPath)
              }
            })
          )
        },
        new Menu("Options") {
          contents ++= Seq(
            new MenuItem(Action("Number of couples") {
              Dialog.showInput(mainFrame, "Number of couples", initial = visualizer.num_couples.toString).foreach { str =>
                str.toIntOption match {
                  case Some(num_couples) if num_couples > 1 =>
                    visualizer = Visualizer(visualizer.dance, num_couples)
                    graphicsPanel.repaint()
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

    def loadDance(path: Path, num_couples: Int = 6): Unit = {
      val dance = Dance.fromYaml(Files.readString(path))
      val new_visualizer = Visualizer(dance, num_couples)
      val inputStream = AudioSystem.getAudioInputStream(path.getParent.resolve(dance.song).toFile)
      val new_song: Clip = AudioSystem.getLine(new DataLine.Info(classOf[Clip], inputStream.getFormat)).asInstanceOf[Clip]
      new_song.open(inputStream)
      progress.max = (new_song.getMicrosecondLength/1000).toInt
      visualizer = new_visualizer
      song = new_song
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
      progress.value = (song.getMicrosecondPosition/1000).toInt
    })

    loadDance(Path.of("dances/Hole in the Wall.yaml"))
  }

  mainFrame.visible = true
}
