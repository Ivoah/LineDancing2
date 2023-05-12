import java.io.File
import java.nio.file.Path
import javax.swing.filechooser.FileNameExtensionFilter
import scala.swing.*

object LineDancing extends MainFrame with App {
  private var visualizer = new Visualizer(Dance.fromYaml(Path.of("dances/Hole in the Wall.yaml")))
  private val mainFrame = this

  menuBar = new MenuBar {
    contents ++= Seq(
      new Menu("File") {
        contents ++= Seq(
          new MenuItem(Action("Load dance") {
            val chooser = new FileChooser(new File("."))
            chooser.fileFilter = new FileNameExtensionFilter("YAML files", "yml", "yaml")
            if (chooser.showOpenDialog(this) == FileChooser.Result.Approve) {
              visualizer.close()
              visualizer = new Visualizer(Dance.fromYaml(chooser.selectedFile.toPath))
              mainFrame.contents = visualizer
            }
          })
        )
      },
      new Menu("Options") {
        contents ++= Seq(
          new MenuItem(Action("Number of couples") {
            Dialog.showInput(mainFrame, "Number of couples", initial = visualizer.NUM_COUPLES.toString).foreach { str =>
              str.toIntOption match {
                case Some(dancers) if dancers > 1 =>
                  visualizer.close()
                  visualizer = new Visualizer(visualizer.dance, dancers)
                  mainFrame.contents = visualizer
                case _ => Dialog.showMessage(mainFrame, s""""$str" is not a valid option""", "Error", Dialog.Message.Error)
              }
            }
          })
        )
      },
      new MenuItem(Action("Play/Pause") {
        if (visualizer.song.isRunning) visualizer.pause() else visualizer.play()
      })
    )
  }

  contents = visualizer
  title = "Line Dancing"
  open()
  // visualizer.play()
}
