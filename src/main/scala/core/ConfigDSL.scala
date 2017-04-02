package core

/**
  * Created by lukmy on 19.03.2017.
  */

import java.io.FileReader

import core.config.ConfigEntry

import scala.util.Try
import scala.util.parsing.combinator._


class ConfigDSL extends JavaTokenParsers {

  def configuration = rep(expression)

  def expression = assignment | extension

  def assignment = ident ~ ":=" ~ stringLiteral ^^ { case (a ~ b ~ c) => MapEntry(a, c) }

  def extension = ident ~ "+=" ~ rep(dependency) ^^ { case (a ~ b ~ c) => MapEntry(a, DependencyList(c)) }

  def dependency = stringLiteral ~ "%%" ~ stringLiteral ~ "%" ~ stringLiteral ^^ { case (a ~ b ~ c ~ d ~ e) => Dependency(a, c, e) }

  implicit def toPureString(value: String): PureString = PureString(value.substring(1, value.length() - 1))

}

object ConfigDSL extends ConfigDSL {

  def parseConfigFile(uri: String): Try[Map[String, ConfigValue]] = Try {
    parseAll(configuration, new FileReader(uri)) match {
      case Success(res, _) => res.map(f => (f.key, f.value)).toMap
      case Failure(res, _) => throw new ConfigFileException(res.toString)
    }
  }
}

sealed trait ConfigValue

case class PureString(value: String) extends ConfigValue

case class Dependency(group: String, artifact: String, version: String) extends ConfigValue

case class DependencyList(dependencies: List[Dependency]) extends ConfigValue

case class MapEntry(key: String, value: ConfigValue)