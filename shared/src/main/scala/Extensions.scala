
object Extensions {
  import Numeric.Implicits.*
  extension[A: Numeric, B: Numeric](t: (A, B)) {
    def +(other: (A, B)): (A, B) = (t._1 + other._1, t._2 + other._2)
    def *(other: (A, B)): (A, B) = (t._1 * other._1, t._2 * other._2)
  }
  
  extension[A: Numeric, B: Numeric, C: Numeric](tt: ((A, B), C)) {
    def +(other: ((A, B), C)): ((A, B), C) = (tt._1 + other._1, tt._2 + other._2)
  }
}
