trait AbstractDrawingContext {
  val width: Int
  val height: Int

  def clear(): Unit

  def save(): Unit
  def restore(): Unit

  def setColor(color: Color): Unit

  def translate(pos: (Double, Double)): Unit
  def rotate(angle: Double): Unit

  def drawString(str: String, x: Int, y: Int): Unit
  def drawOval(x: Int, y: Int, w: Int, h: Int): Unit
}
