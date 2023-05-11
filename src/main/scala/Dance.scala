import java.io.{InputStream, BufferedInputStream}
import javax.sound.sampled._
import scala.io.Source
import org.virtuslab.yaml.{YamlCodec, StringOps}

object Dance {
  def fromYaml(file: InputStream): Dance = {
    case class DanceYaml(song: String, marks: Seq[Int], steps: Seq[String]) derives YamlCodec
    val source = Source.fromInputStream(file).getLines().mkString("\n")
    val yaml = source.as[DanceYaml].toOption.get

    var last = 0
    val steps = yaml.steps.flatMap {
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

    val inputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(getClass.getResourceAsStream(yaml.song)))
    val clip: Clip = AudioSystem.getLine(new DataLine.Info(classOf[Clip], inputStream.getFormat)).asInstanceOf[Clip]
    clip.open(inputStream)

    // val volume = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl]
    // volume.setValue(volume.getMinimum)

    Dance(steps, clip, yaml.marks, last)
  }
}

case class Dance(steps: Map[Range, Seq[(String, Steps.Step)]], song: Clip, marks: Seq[Int], length: Int) {
  private val mark_fns = marks.sliding(2).zipWithIndex.map {
    case (Seq(a, b), i) => (b, (ms: Double) => math.max((ms - a)*length/(b - a) + length*i, 0))
  }.toSeq

  def ms_to_count(ms: Double): Double = {
    mark_fns.find(ms < _._1).map(_._2).getOrElse(_ => length*(marks.length - 1))(ms)
  }

  // def count_to_ms(count: Double): Int = (count*speed).toInt + start

  def range_at(count: Double): Option[Range] = steps.keys.find(range => range.start <= count%length && count%length < range.end)
}
