import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import scala.swing._

object LineDancing extends MainFrame with App {
  val visualizer = new Visualizer(Dance(getClass.getResourceAsStream(s"Hole in the Wall.txt")))

  menuBar = new MenuBar {
    contents ++= Seq(
//      new Menu("File") {
//        contents ++= Seq(
//          new MenuItem(Action("Load MIDI") {
//            val chooser = new FileChooser(new File("."))
//            chooser.fileFilter = new FileNameExtensionFilter("MIDI files", "mid", "midi")
//            if (chooser.showOpenDialog(this) == FileChooser.Result.Approve) {
//              visualizer.loadSequence(MidiSystem.getSequence(chooser.selectedFile))
//            }
//          })
//        )
//      },
//      new Menu("Options") {
//        contents ++= Seq(
//          new MenuItem(Action("Width") {
//
//          }),
//          new MenuItem(Action("Height") {
//
//          }),
//          new MenuItem(Action("Samples") {
//
//          })
//        )
//      },
      new MenuItem(Action("Play/Pause") {
        if (visualizer.dance.song.isRunning) visualizer.dance.song.stop() else visualizer.dance.song.start()
      })
    )
  }

  contents = visualizer
  title = "Line Dancing"
  open()
  // visualizer.play()
}
