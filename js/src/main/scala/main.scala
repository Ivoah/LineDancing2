import Extensions.*

import org.scalajs.dom.*
import org.scalajs.dom.html.{Canvas, Div, Button}

import scala.scalajs.js.timers.*
import scala.scalajs.js.Date

import scalatags.JsDom.all.*

@main
def main(): Unit = {
  val canvas = scalatags.JsDom.all.canvas(
    widthA:=640,
    heightA:=480,
    display:="none"
  ).render
  val dpr = window.devicePixelRatio
  canvas.width = (canvas.width*dpr).toInt
  canvas.height = (canvas.height*dpr).toInt
  val canvasCtx = CanvasDrawingContext({
    val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    ctx.scale(dpr, dpr)
    ctx
  }, dpr)
  val svgContainer = div.render
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
  val visualizer = Visualizer(dance, 6)

  val audioElement = audio(
    attr("controls").empty,
    attr("preload"):="auto",
    source(src:="dances/Hole in the Wall.wav")
  ).render

  var lastTime = -1.0
  def updateAnimation(ts: Double): Unit = {
    if (audioElement.currentTime != lastTime) {
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

    lastTime = audioElement.currentTime
    window.requestAnimationFrame(updateAnimation _)
  }

  window.requestAnimationFrame(updateAnimation _)

  val clickBox = div(
    `id`:="clickBox",
    onclick := { () =>
      if (audioElement.paused) audioElement.play()
      else audioElement.pause()
    }
  ).render

  val canvasBtn = button(
    "canvas",
    onclick := { () =>
      lastTime = -1.0
      svgContainer.style.display = "none"
      canvas.style.display = ""
    }
  ).render

  val svgBtn = button(
    "svg",
    onclick := { () =>
      lastTime = -1.0
      canvas.style.display = "none"
      svgContainer.style.display = ""
    }
  ).render

  document.body.append(
    canvasBtn,
    svgBtn,
    div(
      clickBox,
      canvas,
      svgContainer
    ).render,
    audioElement
  )
}
