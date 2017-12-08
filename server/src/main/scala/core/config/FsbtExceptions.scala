package core.config


object FsbtExceptions{

  case class ConfigFileException(content: String, input: ConfigDSL.Input) extends Exception{
    override def toString: String = super.toString + content + s"Input: $input"
  }

  class ConfigFileValidationException(reason: String) extends Exception{
    override def toString: String = s"build.fsbt $reason"
  }
}

