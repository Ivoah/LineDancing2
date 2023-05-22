import Extensions.*

import org.scalajs.dom.*
import org.scalajs.dom.html.{Canvas, Div}

import scala.scalajs.js.timers.*
import scala.scalajs.js.Date

@main
def main(): Unit = {
  val num_couples = 6
  val canvas = document.querySelector("canvas").asInstanceOf[Canvas]
  implicit val ctx = CanvasDrawingContext(canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
  val svgContainer = document.getElementById("container").asInstanceOf[Div]

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
  val visualizer = Visualizer(dance)

  val audioElement = document.querySelector("audio").asInstanceOf[Audio]
  var lastDraw = 0.0
  setInterval(10) {
    ctx.clear()

    visualizer.draw(audioElement.currentTime*1000)
    ctx.drawString(f"${1000/((Date.now() - lastDraw))}%2.2f", ctx.width - 50, 20)

    val svgCtx = SvgDrawingContext(640, 480)
    visualizer.draw(audioElement.currentTime*1000)(svgCtx)
    svgCtx.drawString(f"${1000/((Date.now() - lastDraw))}%2.2f", svgCtx.width - 50, 20)

    svgContainer.innerHTML = svgCtx.render()
    lastDraw = Date.now()
  }

  canvas.onclick = { _ =>
    if (audioElement.paused) audioElement.play()
    else audioElement.pause()
    audioElement.focus(new FocusOptions{focusVisible = false})
  }
}
