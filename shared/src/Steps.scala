import Extensions.*

import scala.math.*
import scala.util.matching.Regex

import net.ivoah.lisp

object Steps {
  type Step = (Dancer, Double, Double) => Option[((Double, Double), Double)]

  private val lispSteps = Map(
    "Sit" -> "nil",
    raw"(?<couple>1st|2nd) couple cast (?<direction>up|down) (?<places>\d+)" -> """
      (fn (dancer count t)
        (if (= (= (% ((. dancer couple) count) 2) 0) (= couple "1st"))
          (seq
            (* (+ (/ (- (cos (* t Pi))) 2) 0.5) (float places) (if (= direction "up") -1 1))
            (* (/ (sin (* t Pi)) 3) (if (. dancer woman) -1 1))
            (* (- t) Pi 2 (if (. dancer woman) -1 1) (if (= direction "up") -1 1))
          )
          nil
        )
      )""",
    raw"Turn single (?<direction>left|right)" -> """
      (fn (dancer count t)
        (match direction
          "left" (seq 0 0 (- (* t 2 Pi)))
          "right" (seq 0 0 (* t 2 Pi))
        )
      )""",
      // raw"(?<couple>1st|2nd) couple lead (?<direction>up|down) (?<places>\d+)" -> """
      //   (fn (dancer count t)
      //     (if (= (= (% ((. dancer couple) count) 2) 0) (= couple "1st"))
      //       (seq
      //         (* (+ (/ (- (cos (* t Pi))) 2) 0.5) (int places) (if (= direction "up") -1 1))
      //         (* (- (sin (* t Pi))) (/ 1 3) (if (. dancer woman) -1 1))
      //         (* (sin (* t Pi)) Pi (/ 1 2) (if (. dancer woman) -1 1) (if (= direction "up") -1 1))
      //       )
      //       nil
      //     )
      //   )"""
  ).map { case (re, code) =>
    val ast = lisp.parse(code)
    re.r -> ((meta: Map[String, String]) => {
      val fn = ast.eval(lisp.stdlib ++ meta).asInstanceOf[Function3[Map[String, lisp.Value], Double, Double, Seq[Double]]]
      (dancer: Dancer, count: Double, t: Double) => Option(fn(Map("couple" -> dancer.couple, "woman" -> dancer.woman), count, t)).map{case Seq(x, y, r) => ((x, y), r)}
    })
  }
  
  private val scalaSteps = Map[Regex, Map[String, String] => Step](
    // raw"Sit" -> ((meta: String => String) => (dancer: Dancer, count: Double, t: Double) => {
    //   None
    // }),

    // raw"(?<couple>1st|2nd) couple cast (?<direction>up|down) (?<places>\d+)" -> ((meta: String => String) => (dancer: Dancer, count: Double, t: Double) => {
    //   if (dancer.couple(count)%2 == 0 == (meta("couple") == "1st")) Some((
    //       (
    //         (-cos(t * Pi) / 2 + 0.5) * meta("places").toInt * (if (meta("direction") == "up") -1 else 1),
    //         sin(t * Pi) / 3 * (if (dancer.woman) -1 else 1)
    //       ),
    //       -t * math.Pi*2 * (if (dancer.woman) -1 else 1) * (if (meta("direction") == "up") -1 else 1)
    //     ))
    //   else None
    // }),

    raw"(?<couple>1st|2nd) couple lead (?<direction>up|down) (?<places>\d+)".r -> ((meta: Map[String, String]) => (dancer: Dancer, count: Double, t: Double) => {
      if (dancer.couple(count)%2 == 0 == (meta("couple") == "1st")) Some((
          (
            (-cos(t*Pi)/2 + 0.5)*meta("places").toInt*(if (meta("direction") == "up") -1 else 1),
            -sin(t*Pi)/3*(if (dancer.woman) -1 else 1)
          ),
          sin(t*Pi)*math.Pi/2*(if (dancer.woman) -1 else 1)*(if (meta("direction") == "up") -1 else 1)
        ))
      else None
    }),

    raw"(?<corners>1st|2nd) corners cross right shoulders".r -> ((meta: Map[String, String]) => (dancer: Dancer, count: Double, t: Double) => {
      ((dancer.couple(count)%2, dancer.woman, meta("corners")) match {
        case (1, true, "1st")  => Some((1.0, 1.0))
        case (0, false, "1st") => Some((-1.0, -1.0))
        case (0, true, "2nd")  => Some((-1.0, 1.0))
        case (1, false, "2nd") => Some((1.0, -1.0))
        case _ => None
      }).map(corner => (
        (cos(t*Pi/2) - 1, sin(t*Pi/2))*corner,
        -t*math.Pi*(if (meta("corners") == "1st") -1 else 1)
      ))
    }),

    raw"Circle (?<direction>left|right) halfway".r -> ((meta: Map[String, String]) => (dancer: Dancer, count: Double, t: Double) => {
      Some(((dancer.couple(count)%2, dancer.woman) match {
        case (1, false) => (sin(t*Pi/2), -cos(t*Pi/2) + 1)
        case (0, false) => (cos(t*Pi/2) - 1, sin(t*Pi/2))
        case (0, true)  => (-sin(t*Pi/2), cos(t*Pi/2) - 1)
        case (1, true)  => (-cos(t*Pi/2) + 1, -sin(t*Pi/2))
        case _ => (0, 0) // Silence non-exhaustive match warning
      }, t*math.Pi))
    }),

    raw"(?<corners>1st|2nd) corners back-to-back".r -> ((meta: Map[String, String]) => (dancer: Dancer, count: Double, t: Double) => {
      None
    })

    // raw"Turn single (?<direction>left|right)" -> ((meta: Map[String, String]) => (dancer: Dancer, count: Double, t: Double) => {
    //   Some(((0, 0), meta("direction") match {
    //     case "left" => -t*2*Pi
    //     case "right" => t*2*Pi
    //   }))
    // })
  )
  
  val steps: Map[Regex, Map[String, String] => Step] = scalaSteps ++ lispSteps
}

@main
def testSteps() = {
  implicit val ctx = NullDrawingContext(640, 480)
  val dance = Dance.fromYaml("""
  song: Hole in the Wall.wav
  marks: [1600, 33400, 66000, 98640, 131360, 162800, 194600, 226000]

  steps:
    - 1st couple cast down 2 (4 counts)
    - 1st couple lead up 2 (8 counts)
    - 2nd couple cast up 2 (4 counts)
    - 2nd couple lead down 2 (8 counts)
    - 1st corners cross right shoulders (6 counts)
    - 2nd corners cross right shoulders (6 counts)
    - Circle left halfway (6 counts)
    - 1st couple cast down 1 while 2nd couple lead up 1 (6 counts)
  """)
  val visualizer = Visualizer(dance, 6)
  visualizer.draw(dance.marks.head + 1)
}
