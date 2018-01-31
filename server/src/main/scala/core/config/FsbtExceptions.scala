package core.config

import core.config.FsbtModule.FsbtProjectRef


object FsbtExceptions{

  case class ConfigFileException(content: String, input: ConfigDSL.Input) extends Exception{
    override def toString: String = super.toString + content + s"Input: $input"
  }

  class ConfigFileValidationException(reason: String) extends Exception{
    override def toString: String = s"build.fsbt $reason"
  }

  class ModuleNotFoundException(failed: List[(FsbtModule, FsbtProjectRef)]) extends Throwable{

    override def getMessage: String = toString

    override def toString: String =
      "Failed to locate modules: " +
      failed.map(p => s"${p._1.projectName} depends on module ${p._2} but it was not found")
      .foldLeft("")((z, op) => {s"$z \n $op"})
  }
}

