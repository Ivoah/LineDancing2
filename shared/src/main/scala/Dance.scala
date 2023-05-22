import org.virtuslab.yaml.{StringOps, YamlCodec}

object Dance {
  def fromYaml(yaml: String): Dance = {
    case class DanceYaml(song: String, marks: Seq[Int], steps: Seq[String]) derives YamlCodec
    val dance = yaml.as[DanceYaml].toOption.get

    var last = 0
    val steps = dance.steps.flatMap {
      case s"${steps_s} (${counts_s} counts)" =>
        val counts = counts_s.toInt
        val steps = steps_s.split(" while ").map { step =>
          Steps.steps.flatMap { case (regex, fn) =>
            regex.findFirstMatchIn(step).map(m => (step, fn(m.group)))
          }.head
        }.toSeq
        val pair = (last until last + counts) -> steps
        last += counts
        Some(pair)
    }.toMap

    Dance(steps, dance.song, dance.marks, last)
  }
}

case class Dance(steps: Map[Range, Seq[(String, Steps.Step)]], song: String, marks: Seq[Int], length: Int) {
  private val mark_fns = marks.sliding(2).zipWithIndex.map {
    case (Seq(a, b), i) => (b, (ms: Double) => math.max((ms - a)*length/(b - a) + length*i, 0))
  }.toSeq

  def ms_to_count(ms: Double): Double = {
    mark_fns.find(ms < _._1).map(_._2).getOrElse(_ => length*(marks.length - 1))(ms)
  }

  // def count_to_ms(count: Double): Int = (count*speed).toInt + start

  def range_at(count: Double): Option[Range] = steps.keys.find(range => range.start <= count%length && count%length < range.end)
}

