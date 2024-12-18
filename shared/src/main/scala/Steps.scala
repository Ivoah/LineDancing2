import Extensions.*

import scala.math.*
import scala.util.matching.Regex

object Steps {
  type Step = (Dancer, Double, Double) => Option[((Double, Double), Double)]

  val steps: Map[Regex, (String => String) => Step] = Map[Regex, (String => String) => Step](
    raw"Sit".r -> ((meta: String => String) => (dancer: Dancer, count: Double, t: Double) => {
      None
    }),

    raw"(?<couple>1st|2nd) couple cast (?<direction>up|down) (?<places>\d+)".r -> ((meta: String => String) => (dancer: Dancer, count: Double, t: Double) => {
      if (dancer.couple(count)%2 == 0 == (meta("couple") == "1st")) Some((
          (
            (-cos(t * Pi) / 2 + 0.5) * meta("places").toInt * (if (meta("direction") == "up") -1 else 1),
            sin(t * Pi) / 3 * (if (dancer.woman) -1 else 1)
          ),
          -t * math.Pi*2 * (if (dancer.woman) -1 else 1) * (if (meta("direction") == "up") -1 else 1)
        ))
      else None
    }),

    raw"(?<couple>1st|2nd) couple lead (?<direction>up|down) (?<places>\d+)".r -> ((meta: String => String) => (dancer: Dancer, count: Double, t: Double) => {
      if (dancer.couple(count)%2 == 0 == (meta("couple") == "1st")) Some((
          (
            (-cos(t*Pi)/2 + 0.5)*meta("places").toInt*(if (meta("direction") == "up") -1 else 1),
            -sin(t*Pi)/3*(if (dancer.woman) -1 else 1)
          ),
          sin(t*Pi)*math.Pi/2*(if (dancer.woman) -1 else 1)*(if (meta("direction") == "up") -1 else 1)
        ))
      else None
    }),

    raw"(?<corners>1st|2nd) corners cross right shoulders".r -> ((meta: String => String) => (dancer: Dancer, count: Double, t: Double) => {
      ((dancer.couple(count)%2, dancer.woman, meta("corners")) match {
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

    raw"Circle (?<direction>left|right) halfway".r -> ((meta: String => String) => (dancer: Dancer, count: Double, t: Double) => {
      Some(((dancer.couple(count)%2, dancer.woman) match {
        case (1, false) => (sin(t*Pi/2), -cos(t*Pi/2) + 1)
        case (0, false) => (cos(t*Pi/2) - 1, sin(t*Pi/2))
        case (0, true) => (-sin(t*Pi/2), cos(t*Pi/2) - 1)
        case (1, true) => (-cos(t*Pi/2) + 1, -sin(t*Pi/2))
      }, t*math.Pi))
    }),

    raw"Turn single (?<direction>left|right)".r -> ((meta: String => String) => (dancer: Dancer, count: Double, t: Double) => {
      Some(((0, 0), meta("direction") match {
        case "left" => -t*2*Pi
        case "right" => t*2*Pi
      }))
    })
  )
}
