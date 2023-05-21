
case class Color(r: Int, g: Int, b: Int, a: Int = 255) {
    private val BRIGHT_SCALE = 0.7
    override def toString(): String = s"rgba($r, $g, $b, ${a.toDouble/255})"
    def withAlpha(newAlpha: Int) = Color(r, g, b, newAlpha)
    def darker = Color((r*BRIGHT_SCALE).toInt, (g*BRIGHT_SCALE).toInt, (b*BRIGHT_SCALE).toInt, a)
}

object Color {
    val BLACK = Color(0, 0, 0)
    val WHITE = Color(255, 255, 255)
}
