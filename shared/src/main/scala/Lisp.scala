package net.ivoah.lisp

import scala.collection.mutable.{Stack, ListBuffer}

given fnConversion[A, B]: Conversion[A => B, Function] = f => Function { case Seq(a1: A) => f(a1).asInstanceOf[Value] }
case class Function(fn: Seq[Value] => Value)

// type Value = Null | Boolean | Double | String | Function
type Value = Any

type Environment = Map[String, Value]

sealed trait Expr {
  def eval(implicit env: Environment): Value
}

case class LispList(elements: Expr*) extends Expr {
  override def toString(): String = elements.mkString("(", " ", ")")

  override def eval(implicit env: Environment): Value = {
    val args = elements.tail.map(_.eval)
    elements.head.eval() match {
      case Function(fn) => fn(args)
      case map: Map[Value, Value] if args.length == 1 => map(args.head)
    }
  }
}

case class Atom(value: String) extends Expr {
  override def toString(): String = value.toString

  override def eval(implicit env: Environment): Value = {
    value match {
      case "nil" => null
      case boolean if boolean.toBooleanOption.nonEmpty => boolean.toBoolean
      case number if number.toDoubleOption.nonEmpty => number.toDouble
      case s if s.startsWith("\"") && s.endsWith("\"") => s.substring(1, s.length - 1)
      case identifier if env.contains(identifier) => env(identifier)
    }
  }
}

object Atom {
}

type Token = String
def tokenize(chars: String): Seq[Token] = {
  Iterator.unfold(("", 0)) { case (curTok, i) =>
    chars.lift(i) match {
      case Some(paren) if paren == '(' || paren == ')' => Some(Seq(curTok, paren.toString), ("", i + 1))
      case Some(whitespace) if whitespace.isWhitespace && curTok.nonEmpty => Some(Seq(curTok), ("", i + 1))
      case Some(whitespace) if whitespace.isWhitespace => Some(Seq(), ("", i + 1))
      case Some('"') =>
        val endQuote = chars.indexOf("\"", i + 1)
        Some(Seq(chars.substring(i, endQuote + 1)), ("", endQuote + 1))
      case Some(c) => Some(Seq(), ((curTok + c), i + 1))
      case None if curTok.nonEmpty => Some(Seq(curTok), ("", i))
      case None => None
    }
  }.toSeq.flatten.filter(_.nonEmpty)
}

def read_from_tokens(tokens: Stack[Token]): Expr = {
  if (tokens.isEmpty) throw Exception("Unexpected EOF")
  tokens.pop() match {
    case "(" =>
      val L = ListBuffer[Expr]()
      while (tokens.top != ")") {
        L.append(read_from_tokens(tokens))
      }
      tokens.pop()
      LispList(L.toSeq*)
    case ")" => throw Exception("Unexpected )")
    case token => Atom(token)
  }
}

def parse(code: String): Expr = {
  val tokens = Stack.from(tokenize(code))
  val expr = read_from_tokens(tokens)
  if (tokens.nonEmpty) throw Exception(s"Expected end of input")
  expr
}

def eval(code: String)(implicit env: Environment) = parse(code).eval()

implicit val stdlib: Environment = Map(
  "+" -> Function(_.map(_.asInstanceOf[Double]).sum),
  "-" -> Function {
    case Seq(a: Double) => -a
    case Seq(a: Double, b: Double) => a - b
  },
  "*" -> Function(_.map(_.asInstanceOf[Double]).product),
  "/" -> Function {
    case Seq(a: Double, b: Double) => a/b
  },
  "%" -> Function { case Seq(a: Double, b: Double) => a%b },
  "=" -> Function(args => args.forall(_ == args.head)),
  "if" -> Function { case Seq(condition: Boolean, return1, return2) =>
    if (condition) return1
    else return2
  },
  "map" -> Function { case args if args.length%2 == 0 =>
    args.grouped(2).map { case Seq(key, value) => key -> value }.toMap
  },
  "Pi" -> math.Pi,
  "cos" -> fnConversion(math.cos),
  "sin" -> fnConversion(math.sin)
)

@main
def lispTest() = {
  val code = """
    "foo"
  """
  println(code)
  println(eval(code)(stdlib ++ Map("t" -> 0.5)))
}
