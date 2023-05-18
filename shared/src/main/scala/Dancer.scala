import Extensions.*

trait Dancer(val dance: Dance, val starting_couple: Int, val num_couples: Int, val woman: Boolean) {
  protected def starting_pos(count: Double): ((Double, Double), Double) = {
    val loop = count.toInt/dance.length
    (
      (
        couple(count) + (if (loop%2 != 0) 1 else 0),
        if (woman) 0 else 1
      ),
      if (woman) 0 else math.Pi
    )
  }

  def couple(count: Double): Int = {
    val loop = count.toInt/dance.length
    val offset = if (starting_couple%2 == 0) {
      starting_couple
    } else {
      num_couples*2 - starting_couple - 1
    }

    lazy val f: PartialFunction[Int, Int] = {
      case n if n < num_couples => n
      case n => (num_couples - 1) - f(n - num_couples)
    }
    f(loop + offset) - loop%2
  }

  def sitting(count: Double): Boolean = {
    val loop = count.toInt/dance.length
    couple(count) match {
      case c if c < 0 => true
      case c if num_couples%2 == 0 && loop%2 != 0 && c == num_couples - 2 => true
      case c if num_couples%2 != 0 && loop%2 == 0 && c == num_couples - 1 => true
      case _ => false
    }
  }

  // def draw(count: Double, scale: (Double, Double)): Unit
}
