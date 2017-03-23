package core

class Config() {



}

object Config{

  val sourceRoot = ("sourceDir", "src/")
  val targetRoot = ("targetDir", "target/")
  val version = ("version", "1.0")

  def getDefaultConfig() = {
    Map(sourceRoot, targetRoot, version)
  }



}




