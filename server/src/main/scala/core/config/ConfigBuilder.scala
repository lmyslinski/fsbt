package core.config

import better.files.File
import com.martiansoftware.nailgun.NGContext
import core.dependencies.MavenDependency

object ConfigBuilder {

  def build(workDir: String): FsbtConfig = actualBuildConfig(workDir)

  def build(context: NGContext): FsbtConfig = actualBuildConfig(context.getWorkingDirectory)

  private def actualBuildConfig(workDir: String) = {
    val configFilePath = workDir + "/build.fsbt"
    val config: Map[ConfigEntry.Value, Any] = (for {
      _ <- ConfigValidator.validateConfigFileExists(configFilePath)
      configMap <- ConfigDSL.parseConfigFile(configFilePath)
    } yield buildConfig(configMap, workDir)).get

    val environment: Environment.Value = System.getProperty("os.name").contains("Windows") match {
      case true => Environment.Windows
      case false => Environment.Unix
    }

    new FsbtConfig(
      config(ConfigEntry.dependencyList).asInstanceOf[List[Dependency]].map(new MavenDependency(_)),
      File(config(ConfigEntry.targetDirectory).toString),
      config(ConfigEntry.workingDir).toString, config(ConfigEntry.name).toString,
      environment)
  }


  // TODO make this actually make sense
  private def buildConfig(configMap: Map[ConfigEntry.Value, ConfigValue], workDir: String): Map[ConfigEntry.Value, Any] = {

    val defaultConfig = Map(
      (ConfigEntry.workingDir, workDir),
      (ConfigEntry.sourceDirectory, workDir + "/src/"),
      (ConfigEntry.targetDirectory, workDir + "/target/"),
      (ConfigEntry.version, "1.0"),
      (ConfigEntry.name, ""),
      (ConfigEntry.dependencyList, "")
    )

    defaultConfig.map((keyValue) => {
      val key = keyValue._1
      if (configMap.keySet.contains(key)) {
        configMap(key) match {
          case PureString(value) => (key, value)
          case DependencyList(list) => (key, list)
        }
      } else {
        keyValue
      }
    })
  }
}

