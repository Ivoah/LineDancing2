import java.io.{InputStream, BufferedInputStream}
import javax.sound.sampled._
import scala.io.Source

object Dance {
  def apply(file: InputStream): Dance = {
    val source = Source.fromInputStream(file)
    val lines = source.getLines()

    val song = lines.next() match { case s"Song: ${song}" => song }
    val start = lines.next() match { case s"Start: ${start}" => start.toInt }
    val speed = lines.next() match { case s"Speed: ${speed}" => speed.toDouble }

    var last = 0
    lines.next()
    val steps = lines.flatMap {
      case s"Comment: $s" => None
      case s"${steps_s} (${counts_s} counts)" =>
        val counts = counts_s.toInt
        val steps = steps_s.split(" while ").map { step =>
          Steps.steps.flatMap { case (regex, fn) =>
            regex.findFirstMatchIn(step).map(m => (step, fn(m.groupNames.map(name => (name, m.group(name))).toMap)))
          }.head
        }.toSeq
        val pair = (last until last + counts) -> steps
        last += counts
        Some(pair)
    }.toMap

    val inputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(getClass.getResourceAsStream(song)))
    val clip: Clip = AudioSystem.getLine(new DataLine.Info(classOf[Clip], inputStream.getFormat)).asInstanceOf[Clip]
    clip.open(inputStream)

//    val volume = clip.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl]
//    volume.setValue(volume.getMinimum)


    Dance(steps, clip, start, speed, last)
  }
}

case class Dance(val steps: Map[Range, Seq[(String, Steps.Step)]], song: Clip, start: Int, speed: Double, length: Int) {
  def ms_to_count(ms: Double): Double = (ms - start)/speed
  def count_to_ms(count: Double): Int = (count*speed).toInt + start
  def range_at(count: Double): Option[Range] = steps.keys.find(range => range.start <= count%length && count%length < range.end)
}
