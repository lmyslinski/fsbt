package core.config

class ConfigFileException(content: String) extends Exception{
  override def toString: String = super.toString + content
}
