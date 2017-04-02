package core

import scala.util.Try

class Config() {



  def init(configFileUriOption : Option[String]) = {
//    "C:\\Dev\\fsbt\\testProject\\build.fsbt"
    val configFileUri = if (configFileUriOption.isDefined) configFileUriOption.get else Config.defaultConfigFileUri

    for{
      configMap <- ConfigDSL.parseConfigFile(configFileUri)
      configMap <- ConfigValidator.validateConfigFileKeySet(configMap)
      configMap <- ConfigValidator.validateConfigFileValues(configMap)
      config <- buildConfig(configMap)
    } yield config

  }

  def buildConfig(configMap: Map[String, ConfigValue]) = {
    Config.defaultConfig.map( (keyValue) => {
      configMap(keyValue._1) match {
        case PureString(value) => value
        case DependencyList(list) => list
      }
    })
  }



}

object Config{

  val defaultConfigFileUri = System.getProperty("user.dir")

  val defaultConfig = Map(
    ("sourceDir", "src/"),
    ("targetDir", "target/"),
    ("version", "1.0"),
    ("name", ""),
    ("libraryDependencies", "")
  )
}




