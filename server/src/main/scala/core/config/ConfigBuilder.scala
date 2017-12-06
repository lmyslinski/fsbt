package core.config

import better.files.File
import com.martiansoftware.nailgun.NGContext
import core.FsbtUtil.stripQuotes
import core.dependencies.{MavenDependency, MavenDependencyScope}

import scala.util.{Failure, Success}

object ConfigBuilder {

  def build(workDir: String): FsbtProject = stage0(workDir)

  def build(context: NGContext): FsbtProject = stage0(context.getWorkingDirectory)

  private def stage0(workDir: String) = {
    val configFilePath = workDir + "/build.fsbt"

    ConfigDSL.parseConfigFile(configFilePath) match {
      case Success(configValues) => stage1(configValues, configFilePath, workDir)
      case Failure(ex) => throw ex
    }
  }

  private def stage1(configEntries: List[Any], configFilePath: String, workDir: String) = {
    val variables = configEntries.collect { case Variable(key: String, value: String) => (key, value) }.toMap
    val dependencies = configEntries.collect { case DependencyList(deps) => deps }.flatten.map(parseDependency(_, variables))

    val name = if (variables.contains("name")) {
      variables("name")
    } else {
      throw new ConfigFileValidationException("must contain a \"name\" variable")
    }

    val environment: Environment.Value = if (System.getProperty("os.name").contains("Windows")) {
      Environment.Windows
    } else {
      Environment.Unix
    }

    val modules = getModules

    FsbtProject(dependencies, workDir, File(workDir + "/target/"), name, environment, variables, modules)
  }

  private def getModules: List[FsbtModule] = {
    List()
  }

  private def resolveVariable(x: ValueOrVariable, variables: Map[String, String]) = {
    x match {
      case Value(rawValue) => rawValue
      case VariableCall(key) =>
        if (variables.contains(key))
          variables(key)
        else
          throw new ConfigFileValidationException(s"""does not have a \"$key\" variable"""")
    }
  }

  private def parseDependency(dep: Dependency, variables: Map[String, String]) = {
    val artifact = stripQuotes(resolveVariable(dep.artifact, variables))
    val group = stripQuotes(resolveVariable(dep.group, variables))
    val version = stripQuotes(resolveVariable(dep.version, variables))
    val withScalaDeps = if (dep.withScalaVersion == "%%") true else false
    val scope = if (dep.scope.isDefined) {
      MavenDependencyScope.withName(stripQuotes(resolveVariable(dep.scope.get, variables)))
    } else MavenDependencyScope.Compile
    new MavenDependency(group, artifact, version, false, withScalaDeps, scope)
  }


  //    val config: Map[ConfigEntry.Value, Any] = (for {
  //      _ <- ConfigValidator.validateConfigFileExists(configFilePath)
  //      configMap <- ConfigDSL.parseConfigFile(configFilePath)
  //    } yield buildConfig(configMap, workDir)).get
  //


  //    new FsbtConfig(
  //      config(ConfigEntry.dependencyList).asInstanceOf[List[Dependency]].map(new MavenDependency(_)),
  //      File(config(ConfigEntry.targetDirectory).toString),
  //      config(ConfigEntry.workingDir).toString, config(ConfigEntry.name).toString,
  //      environment)


  //  val withScalaVersion = if (scalaVer.length == 2) true else false
  //  val scope = if(scope0.isDefined){
  //    scope0.get._2
  //  }else{
  //    "compile"
  //  }


  // TODO make this actually make sense
  //  private def buildConfig(configMap: Map[ConfigEntry.Value, ConfigValue], workDir: String): Map[ConfigEntry.Value, Any] = {
  //
  //    val defaultConfig = Map(
  //      (ConfigEntry.workingDir, workDir),
  //      (ConfigEntry.sourceDirectory, workDir + "/src/"),
  //      (ConfigEntry.targetDirectory, workDir + "/target/"),
  //      (ConfigEntry.version, "1.0"),
  //      (ConfigEntry.name, ""),
  //      (ConfigEntry.dependencyList, "")
  //    )
  //
  //    defaultConfig.map((keyValue) => {
  //      val key = keyValue._1
  //      if (configMap.keySet.contains(key)) {
  //        configMap(key) match {
  //          case PureString(value) => (key, value)
  //          case DependencyList(list) => (key, list)
  //          case Modules(list) => (key, list)
  //        }
  //      } else {
  //        keyValue
  //      }
  //    })
  //  }
}

