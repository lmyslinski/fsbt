package core.config

import better.files.File
import com.martiansoftware.nailgun.NGContext
import core.dependencies.MavenDependency

object ConfigBuilder{

  def build(context: NGContext) = {
    val configFilePath = context.getWorkingDirectory + "/build.fsbt"
    val config: Map[ConfigEntry.Value, Any] = (for{
      configFileUri <- ConfigValidator.validateConfigFileExists(configFilePath)
      configMap <- ConfigDSL.parseConfigFile(configFilePath)
      configMap <- ConfigValidator.validateConfigFileKeySet(configMap)
      configMap <- ConfigValidator.validateConfigFileValues(configMap)
    } yield buildConfig(configMap, context)).get

    new FsbtConfig(
      config(ConfigEntry.dependencyList).asInstanceOf[List[Dependency]].map(new MavenDependency(_)),
      File(config(ConfigEntry.targetDirectory).toString),
      config(ConfigEntry.workingDir).toString, config(ConfigEntry.name).toString)
  }

  private def buildConfig(configMap: Map[ConfigEntry.Value, ConfigValue], context: NGContext): Map[ConfigEntry.Value, Any] = {

    val defaultConfig = Map(
      (ConfigEntry.workingDir, context.getWorkingDirectory),
      (ConfigEntry.sourceDirectory, context.getWorkingDirectory + "/src/"),
      (ConfigEntry.targetDirectory, context.getWorkingDirectory + "/target/"),
      (ConfigEntry.version, "1.0"),
      (ConfigEntry.name, ""),
      (ConfigEntry.dependencyList, "")
    )

    defaultConfig.map((keyValue) => {
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

