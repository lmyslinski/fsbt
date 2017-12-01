package core.config

import java.io.FileReader

import core.dependencies.MavenDependency

import scala.util.Try
import scala.util.parsing.combinator._

class ConfigDSL extends JavaTokenParsers {

  def configuration = rep(expression)

  def expression = assignment | singleDep | multiDep | unmanagedJar | module

  def assignment = ident ~ ":=" ~ stringLiteral ^^ { case (a ~ b ~ c) => MapEntry(a, c) }

  def singleDep = "libraryDependencies" ~ "+=" ~ rep(dependency) ^^ { case (a ~ b ~ c) => MapEntry(a, DependencyList(c)) }

  def multiDep = "libraryDependencies" ~ "++=" ~ "Seq("  ~ repsep(dependency, ",") ~ ")" ^^ { case (a ~ b ~ c ~ d ~ e) => MapEntry(a, DependencyList(d)) }

  def dependency = dependencyWithScala | dependencyWithoutScala

  def dependencyWithScala = stringLiteral ~ "%%" ~ stringLiteral ~ "%" ~ stringLiteral ^^ { case (a ~ b ~ c ~ d ~ e) => Dependency(a, c, e, withScalaVersion = true) }

  def dependencyWithoutScala = stringLiteral ~ "%" ~ stringLiteral ~ "%" ~ stringLiteral ^^ { case (a ~ b ~ c ~ d ~ e) => Dependency(a, c, e, withScalaVersion = false) }

  def unmanagedJar = "unmanagedJar" ~ "in" ~ jarScope ~ "+=" ~ stringLiteral ^^ {case (a ~ b ~ c ~ d ~ e) => MapEntry(a, UnmanagedJar(e, c))}

  def jarScope = "Compile" | "Runtime" | "Test"

  def module = "submodules" ~ ":=" ~ "(" ~ repsep(stringLiteral, ",") ~ ")" ^^ {case (a ~ b ~ c ~ d ~ e) => MapEntry(a, Modules(d))}

  implicit def toPureString(value: String): PureString = PureString(value.substring(1, value.length() - 1))
}

object ConfigDSL extends ConfigDSL {

  def parseConfigFile(uri: String): Try[Map[ConfigEntry.Value, ConfigValue]] = Try {
    parseAll(configuration, new FileReader(uri)) match {
      case Success(res, _) =>
        res.map(f => (ConfigEntry.withName(f.key), f.value)).toMap
      case Failure(res, ab) =>
        throw new ConfigFileException(res.toString)
    }
  }
}

sealed trait ConfigValue

case class Name(value: String) extends ConfigValue

case class Version(value: String) extends ConfigValue

case class PureString(value: String) extends ConfigValue

case class Dependency(group: String, artifact: String, version: String, withScalaVersion: Boolean) extends ConfigValue

case class DependencyList(dependencies: List[Dependency]) extends ConfigValue

case class UnmanagedJar(path: String, scope: String) extends ConfigValue

case class Modules(moduleList: List[String]) extends ConfigValue

case class MapEntry(key: String, value: ConfigValue)