import org.scalajs.dom.document
import org.scalajs.dom.SVGTextElement
import scala.collection.mutable

case class SvgDrawingContext(override val width: Int, override val height: Int) extends AbstractDrawingContext {
  private val builder = mutable.StringBuilder(s"""<svg width="$width" height="$height" font-family="Eczar" font-size="13px">""")

  def clear(): Unit = {
    builder.clear()
    builder ++= s"""<svg width="$width" height="$height" font-family="Eczar" font-size="13px">"""
  }

  def withColor(color: Color)(thunk: => Unit): Unit = {
    builder ++= s"""<g color="$color" fill="$color">"""
    thunk
    builder ++= "</g>"
  }

  def withTranslation(pos: (Double, Double))(thunk: => Unit): Unit = {
    builder ++= s"""<g transform="translate(${pos._1} ${pos._2})">"""
    thunk
    builder ++= "</g>"
  }
  def withRotation(angle: Double)(thunk: => Unit): Unit = {
    builder ++= s"""<g transform="rotate(${angle*180/math.Pi})">"""
    thunk
    builder ++= "</g>"
  }

  def drawString(str: String, x: Int, y: Int): Unit = builder ++= s"""<text x="$x" y="$y">$str</text>"""
  def drawOval(x: Int, y: Int, w: Int, h: Int): Unit = builder ++= s"""<ellipse x="$x" y="$y" rx="${w/2}" ry="${h/2}" />"""
  def drawRectangle(x: Int, y: Int, w: Int, h: Int): Unit = builder ++= s"""<rect x="$x" y="$y" width="$w" height="$h" />"""

  def stringWidth(str: String): Int = {
    val svg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
    svg.innerHTML = s"""<text font-family="Eczar" font-size="13px">$str</text>"""
    document.body.appendChild(svg)
    val width = svg.children.head.asInstanceOf[SVGTextElement].getComputedTextLength().toInt
    document.body.removeChild(svg)
    width
  }

  def render(): String = {
    builder ++= "</svg>"
    builder.result()
  }
}
