package core.config

import java.io.FileReader

import core.config.FsbtExceptions.ConfigFileException

import scala.util.Try
import scala.util.parsing.combinator.JavaTokenParsers

object ConfigDSL extends JavaTokenParsers {

  // DSL value definitions
  sealed trait ValueOrVarCall
  case class Dependency(group: ValueOrVarCall, artifact: ValueOrVarCall, version: ValueOrVarCall, withScalaVersion: String, scope: Option[ValueOrVarCall])
  case class DependencyList(deps: List[Dependency])
  case class Modules(moduleList: List[String])
  case class Variable(key: String, value: String)
  case class VarCall(name: String) extends ValueOrVarCall
  case class Value(value: String) extends ValueOrVarCall

  def parseConfigFile(uri: String): Try[List[Any]] = Try {
    parseAll(configuration, new FileReader(uri)) match {
      case Success(res, _) => res
      case Failure(res, ab) => throw ConfigFileException(res, ab)
      case x: Error => throw ConfigFileException(x.msg, x.next)
    }
  }

  // DSL parser
  private def configuration = rep(expression)

  private def expression = variable | dependencies | module

  private def variable = stringVariable | intVariable | doubleVariable

  private def stringVariable = ident ~ "=" ~ stringLiteral ^^ { case (a ~ b ~ c) => Variable(a, c) }

  private def intVariable = ident ~ "=" ~ stringLiteral ^^ { case (a ~ b ~ c) => Variable(a, c) }

  private def doubleVariable = ident ~ "=" ~ (decimalNumber | floatingPointNumber) ^^ { case (a ~ b ~ c) => Variable(a, c) }

  private def variableCall = "${" ~ ident ~ "}" ^^ { case (a ~ b ~ c) => VarCall(b) }

  private def literal = stringLiteral ^^ (x => Value(x))

  private def variableOrLiteral = variableCall | literal

  private def dependencies = "dependencies" ~ "=" ~ "{" ~ repsep(dependency, ",") ~ "}" ^^ { case (a ~ b ~ c ~ d ~ e) => DependencyList(d) }

  private def dependency =
    variableOrLiteral ~ ("%%" | "%") ~ variableOrLiteral ~ "%" ~ variableOrLiteral ~ opt("%" ~ variableOrLiteral) ^^ { case (group ~ scalaVer ~ artifact ~ sep ~ version ~ scope0) =>
      val scp = if (scope0.isDefined) {
        Some(scope0.get._2)
      } else None
      Dependency(group, artifact, version, scalaVer, scp)
    }

  private def module = "submodules" ~ "=" ~ "{" ~ repsep(stringLiteral, ",") ~ "}" ^^ { case (a ~ b ~ c ~ d ~ e) => Modules(d) }
}

