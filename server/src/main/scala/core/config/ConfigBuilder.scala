package core.config

import java.io.PrintStream

import better.files.File
import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.FsbtUtil.stripQuotes
import core.config.ConfigDSL.{Dependency, DependencyList, DependentModules, Modules, Value, ValueOrVarCall, VarCall, Variable}
import core.config.FsbtModule.Variables
import core.dependencies.DependencyResolver.resolveAll
import core.dependencies.{MavenDependency, MavenDependencyScope}
import core.config.FsbtExceptions.{ConfigFileValidationException, ModuleNotFoundException}

import scala.annotation.tailrec
import scala.util.{Failure, Success}

object ConfigBuilder{

  def build(workDir: String)(implicit logger: Logger): List[FsbtModule] = stage0(workDir, System.out)

  def buildModules(context: NGContext)(implicit logger: Logger): List[FsbtModule] = stage0(context.getWorkingDirectory, context.out)

  private def stage0(workDir: String, out: PrintStream)(implicit logger: Logger) = {
    val configFilePath = workDir + "/build.fsbt"
    stage1(parseConfigFile(configFilePath), configFilePath, workDir, out)
  }

  private def parseConfigFile(path: String) = {
    ConfigDSL.parseConfigFile(path) match {
      case Success(configValues) => configValues
      case Failure(ex) => throw ex
    }
  }

  private def stage1(configEntries: List[Any], configFilePath: String, workDir: String, out: PrintStream)(implicit logger: Logger): List[FsbtModule] = {
    val variables = configEntries.collect { case Variable(key: String, value: String) => (key, value) }.toMap

    val dependsOn = configEntries.collect{ case DependentModules(mods) => mods.map(stripQuotes)}.flatten

    val dependencies = configEntries.collect { case DependencyList(deps) => deps }.flatten.map(parseDependency(_, variables))

    val name = if (variables.contains("name")) {
      stripQuotes(variables("name"))
    } else {
      throw new ConfigFileValidationException("must contain a \"name\" variable")
    }

    val environment: Environment.Value = if (System.getProperty("os.name").contains("Windows")) {
      Environment.Windows
    } else {
      Environment.Unix
    }

    val modules = getModules(workDir, variables, dependencies, environment, out,
      configEntries.collect { case Modules(moduleList) => moduleList.map(stripQuotes) }.flatten)

    FsbtModule(dependencies,
      workDir,
      File(workDir + "/target/"),
      name,
      environment,
      variables,
      modules.map(_.projectName),
      dependsOn,
      isRootProject = true) :: modules
  }

  private def getModules(workDir: String,
                         variables: Variables,
                         deps: List[MavenDependency],
                         environment: Environment.Value,
                         out: PrintStream,
                         modulesList: List[String])(implicit logger: Logger): List[FsbtModule] = {
    modulesList.flatMap {
      module =>
        val moduleWorkDir = s"$workDir/$module/"
        val configFilePath = moduleWorkDir + "build.fsbt"
        if (File(configFilePath).notExists) {
          out.println(s"""Module \"$module\" is invalid""")
        }

        val configEntries = parseConfigFile(configFilePath)
        val dependsOn = configEntries.collect{ case DependentModules(mods) => mods.map(stripQuotes)}.flatten
        val allVariables = variables ++ configEntries.collect { case Variable(key: String, value: String) => (key, value) }.toMap
        val dependencies = deps ++ configEntries.collect { case DependencyList(d) => d }.flatten.map(parseDependency(_, allVariables))
        val modules = getModules(workDir, allVariables, dependencies, environment, out,
          configEntries.collect { case Modules(moduleList) => moduleList.map(stripQuotes) }.flatten)

        FsbtModule(
          dependencies,
          moduleWorkDir,
          File(moduleWorkDir + "/target/"),
          module,
          environment,
          allVariables,
          modules.map(_.projectName),
          dependsOn) :: modules
    }
  }

  private def resolveVariable(x: ValueOrVarCall, variables: Variables) = {
    x match {
      case Value(rawValue) => rawValue
      case VarCall(key) =>
        if (variables.contains(key))
          variables(key)
        else
          throw new ConfigFileValidationException(s"""does not have a \"$key\" variable"""")
    }
  }

  private def parseDependency(dep: Dependency, variables: Variables) = {
    val artifact = stripQuotes(resolveVariable(dep.artifact, variables))
    val group = stripQuotes(resolveVariable(dep.group, variables))
    val version = stripQuotes(resolveVariable(dep.version, variables))
    val withScalaDeps = if (dep.withScalaVersion == "%%") true else false
    val scope = if (dep.scope.isDefined) {
      MavenDependencyScope.withName(stripQuotes(resolveVariable(dep.scope.get, variables)))
    } else MavenDependencyScope.Compile
    new MavenDependency(group, artifact, version, false, withScalaDeps, scope)
  }
}

