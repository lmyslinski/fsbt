package core.config

object Environment extends Enumeration {
  val Windows, Unix = Value

  def pathSeparator(env: Environment.Value): String = {
    env match{
      case Windows => ";"
      case Unix => ":"
    }
  }

  def dirSeparator(env: Environment.Value): String = {
    env match{
      case Windows => "\\"
      case Unix => "/"
    }
  }
}