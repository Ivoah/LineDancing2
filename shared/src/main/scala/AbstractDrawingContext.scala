trait AbstractDrawingContext {
  val width: Int
  val height: Int

  def clear(): Unit

  def withColor(color: Color)(thunk: => Unit): Unit

  def withTranslation(pos: (Double, Double))(thunk: => Unit): Unit
  def withRotation(angle: Double)(thunk: => Unit): Unit

  def drawString(str: String, x: Int, y: Int): Unit
  def drawOval(x: Int, y: Int, w: Int, h: Int): Unit
}
