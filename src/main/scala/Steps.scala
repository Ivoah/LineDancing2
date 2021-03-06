import scala.math._
import Implicits._

import scala.util.matching.Regex

object Steps {
  type Step = (Dancer, Double) => Option[((Double, Double), Double)]

  val emptyStep: Step = (_, _) => None

  val steps = Map[Regex, Map[String, String] => Step](
    raw"(1st|2nd) couple cast (up|down) (\d+)".r("couple", "direction", "places") -> ((meta: Map[String, String]) => (dancer: Dancer, t: Double) => {
      if (dancer.couple%2 == 0 == (meta("couple") == "1st")) Some((
          (
            (-cos(t * Pi) / 2 + 0.5) * meta("places").toInt * (if (meta("direction") == "up") -1 else 1),
            sin(t * Pi) / 3 * (if (dancer.woman) -1 else 1)
          ),
          -t * math.Pi*2 * (if (dancer.woman) -1 else 1) * (if (meta("direction") == "up") -1 else 1)
        ))
      else None
    }),

    raw"(1st|2nd) couple lead (up|down) (\d+)".r("couple", "direction", "places") -> ((meta: Map[String, String]) => (dancer: Dancer, t: Double) => {
      if (dancer.couple%2 == 0 == (meta("couple") == "1st")) Some((
          (
            (-cos(t*Pi)/2 + 0.5)*meta("places").toInt*(if (meta("direction") == "up") -1 else 1),
            -sin(t*Pi)/3*(if (dancer.woman) -1 else 1)
          ),
          sin(t*Pi)*math.Pi/2*(if (dancer.woman) -1 else 1)*(if (meta("direction") == "up") -1 else 1)
        ))
      else None
    }),

    raw"(1st|2nd) corners cross right shoulders".r("corners") -> ((meta: Map[String, String]) => (dancer: Dancer, t: Double) => {
      ((dancer.couple%2, dancer.woman, meta("corners")) match {
        case (1, true, "1st") => Some((1.0, 1.0))
        case (0, false, "1st") => Some((-1.0, -1.0))
        case (0, true, "2nd") => Some((-1.0, 1.0))
        case (1, false, "2nd") => Some((1.0, -1.0))
        case _ => None
      }).map(corner => (
        (cos(t*Pi/2) - 1, sin(t*Pi/2))*corner,
        -t*math.Pi*(if (meta("corners") == "1st") -1 else 1)
      ))
    }),

    raw"Circle (left|right) halfway".r("direction") -> ((meta: Map[String, String]) => (dancer: Dancer, t: Double) => {
      Some(((dancer.couple%2, dancer.woman) match {
        case (1, false) => (sin(t*Pi/2), -cos(t*Pi/2) + 1)
        case (0, false) => (cos(t*Pi/2) - 1, sin(t*Pi/2))
        case (0, true) => (-sin(t*Pi/2), cos(t*Pi/2) - 1)
        case (1, true) => (-cos(t*Pi/2) + 1, -sin(t*Pi/2))
      }, t*math.Pi))
    })
  )
}
