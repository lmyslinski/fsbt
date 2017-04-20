package core

import core.config.ConfigEntry

class ConfigBuilder(val configFileUri : String = ConfigBuilder.defaultConfigFileUri) {

    val config = (for{
      configFileUri <- ConfigValidator.validateConfigFileExists(configFileUri)
      configMap <- ConfigDSL.parseConfigFile(configFileUri)
      configMap <- ConfigValidator.validateConfigFileKeySet(configMap)
      configMap <- ConfigValidator.validateConfigFileValues(configMap)
    } yield buildConfig(configMap)).get

  def buildConfig(configMap: Map[ConfigEntry.Value, ConfigValue]): Map[ConfigEntry.Value, Any] = {
    ConfigBuilder.defaultConfig.map((keyValue) => {
      val key = keyValue._1
      if(configMap.keySet.contains(key)){
        configMap(key) match {
          case PureString(value) => (key, value)
          case DependencyList(list) => (key, list)
        }
      }else{
        keyValue
      }
    })
  }
}

object ConfigBuilder{

  val defaultConfigFileUri = System.getProperty("user.dir")

  val defaultConfig = Map(
    (ConfigEntry.sourceDirectory, "src/"),
    (ConfigEntry.targetDirectory, "target/"),
    (ConfigEntry.version, "1.0"),
    (ConfigEntry.name, ""),
    (ConfigEntry.dependencyList, "")
  )
}




