package core.config

/**
  * Created by Admin on 30.03.2017.
  */
class ConfigFileException(content: String) extends Exception{
  override def toString: String = super.toString + content
}
