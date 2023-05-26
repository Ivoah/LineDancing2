import Extensions.*

import org.scalajs.dom.*
import org.scalajs.dom.html.{Canvas, Div, Button}

import scala.scalajs.js.timers.*
import scala.scalajs.js.Date

@main
def main(): Unit = {
  val canvas = document.querySelector("canvas").asInstanceOf[Canvas]
  val canvasCtx = CanvasDrawingContext(canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
  val svgContainer = document.getElementById("svgContainer").asInstanceOf[Div]
  val svgCtx = SvgDrawingContext(640, 480)

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

  setInterval(10) {
    if (canvas.style.display != "none") {
      canvasCtx.clear()
      visualizer.draw(audioElement.currentTime*1000)(canvasCtx)
    }

    if (svgContainer.style.display != "none") {
      svgCtx.clear()
      visualizer.draw(audioElement.currentTime*1000)(svgCtx)
      svgContainer.innerHTML = svgCtx.render()
    }
  }

  canvas.onclick = { _ =>
    if (audioElement.paused) audioElement.play()
    else audioElement.pause()
    audioElement.focus(new FocusOptions{focusVisible = false})
  }

  document.getElementById("btnCanvas").asInstanceOf[Button].onclick = { _ =>
    svgContainer.style.display = "none"
    canvas.style.display = ""
  }

  document.getElementById("btnSvg").asInstanceOf[Button].onclick = { _ =>
    canvas.style.display = "none"
    svgContainer.style.display = ""
  }
}
