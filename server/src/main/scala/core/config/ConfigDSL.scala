package core.config

import java.io.FileReader

import scala.util.Try
import scala.util.parsing.combinator._

class ConfigDSL extends JavaTokenParsers {

  def configuration = rep(expression)

  def expression = variable | dependencies | module

  // variable declaration definitions

  def variable = stringVariable | intVariable | doubleVariable

  def stringVariable = ident ~ "=" ~ stringLiteral ^^ { case (a ~ b ~ c) => Variable(a, c) }

  def intVariable = ident ~ "=" ~ stringLiteral ^^ { case (a ~ b ~ c) => Variable(a, c) }

  def doubleVariable = ident ~ "=" ~ (decimalNumber | floatingPointNumber) ^^ { case (a ~ b ~ c) => Variable(a, c) }

  // variable usage

  def variableCall = "${" ~ ident ~ "}" ^^ { case (a ~ b ~ c) => VariableCall(b) }

  def literal = stringLiteral ^^ (x => Value(x))

  def variableOrLiteral = variableCall | literal


  def dependencies = "dependencies" ~ "=" ~ "{" ~ repsep(dependency, ",") ~ "}" ^^ { case (a ~ b ~ c ~ d ~ e) => DependencyList(d) }

  def dependency =
    variableOrLiteral ~ ("%%" | "%") ~ variableOrLiteral ~ "%" ~ variableOrLiteral ~ opt("%" ~ variableOrLiteral) ^^ { case (group ~ scalaVer ~ artifact ~ sep ~ version ~ scope0) =>
      val scp = if (scope0.isDefined) {
        Some(scope0.get._2)
      } else None
      Dependency(group, artifact, version, scalaVer, scp)
    }


  def module = "submodules" ~ "=" ~ "{" ~ repsep(stringLiteral, ",") ~ "}" ^^ { case (a ~ b ~ c ~ d ~ e) => Modules(d) }
}

object ConfigDSL extends ConfigDSL {

  def parseConfigFile(uri: String): Try[List[Any]] = Try {
    parseAll(configuration, new FileReader(uri)) match {
      case Success(res, _) => res
      case Failure(res, ab) => throw ConfigFileException(res, ab)
    }
  }
}


//case class PureString(value: String) extends ConfigValue

sealed trait ConfigValue

case class Dependency(group: ValueOrVariable, artifact: ValueOrVariable, version: ValueOrVariable, withScalaVersion: String, scope: Option[ValueOrVariable]) extends ConfigValue

case class DependencyList(deps: List[Dependency])

case class Modules(moduleList: List[String]) extends ConfigValue

case class Variable(key: String, value: String) extends ConfigValue

sealed trait ValueOrVariable extends ConfigValue

case class VariableCall(name: String) extends ValueOrVariable

case class Value(value: String) extends ValueOrVariable