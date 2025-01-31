import Extensions.*

import org.scalajs.dom.*
import org.scalajs.dom.html.{Canvas, Div, Button}

import scala.scalajs.js.timers.*
import scala.scalajs.js.Date
import scala.scalajs.js.Thenable.Implicits.*
import scala.concurrent.ExecutionContext.Implicits.global

import scalatags.JsDom.all.*

val DANCES_DIR = "dances/"
val DANCES = Seq(
  "dances/Hole in the Wall.yaml",
  "dances/Sunlight Through Draperies.yaml"
)

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

  val audioElement = audio(
    attr("controls").empty,
    attr("preload"):="auto",
  ).render

  var visualizer = Visualizer(Dance.Empty, 6)

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

  def loadDance(path: String): Unit = {
    fetch(path).flatMap(r => r.text()).foreach { t =>
      val dance = Dance.fromYaml(t)
      audioElement.src = DANCES_DIR + dance.song
      audioElement.load()
      visualizer = Visualizer(dance, visualizer.num_couples)
      lastTime = -1.0
    }
  }

  val danceSelect = frag(
    label(`for`:="danceSelect", "Dance: "),
    select(
      `id`:="danceSelect",
      DANCES.map(d => option(`value`:=d, d.stripPrefix(DANCES_DIR).stripSuffix(".yaml"))),
      onchange := { (e: Event) => loadDance(e.currentTarget.asInstanceOf[HTMLSelectElement].value)}
    )
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

  val clickBox = div(
    `id`:="clickBox",
    onclick := { () =>
      if (audioElement.paused) audioElement.play()
      else audioElement.pause()
    }
  ).render

  loadDance(DANCES.head)
  window.requestAnimationFrame(updateAnimation _)

  document.body.append(
    danceSelect,
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
