trait AbstractDrawingContext {
  val width: Int
  val height: Int

  def withColor(color: Color)(thunk: => Unit): Unit
  def withShadow(thunk: => Unit): Unit

  def withTranslation(pos: (Double, Double))(thunk: => Unit): Unit
  def withRotation(angle: Double)(thunk: => Unit): Unit

  def drawString(str: String, x: Int, y: Int): Unit
  def drawRectangle(x: Int, y: Int, w: Int, h: Int): Unit
  def drawOval(x: Int, y: Int, w: Int, h: Int): Unit

  def stringWidth(str: String): Int
  // def stringHeight(str: String): Int
}

case class NullDrawingContext(val width: Int, val height: Int) extends AbstractDrawingContext {
  override def withColor(color: Color)(thunk: => Unit): Unit = thunk
  override def withShadow(thunk: => Unit): Unit = thunk

  override def withTranslation(pos: (Double, Double))(thunk: => Unit): Unit = thunk
  override def withRotation(angle: Double)(thunk: => Unit): Unit = thunk
  override def drawString(str: String, x: Int, y: Int): Unit = ()
  override def drawRectangle(x: Int, y: Int, w: Int, h: Int): Unit = ()
  override def drawOval(x: Int, y: Int, w: Int, h: Int): Unit = ()
  override def stringWidth(str: String): Int = str.length*5
}
