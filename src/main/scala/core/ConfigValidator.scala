package core

import scala.util.{Failure, Success, Try}

/**
  * Created by Admin on 30.03.2017.
  */
object ConfigValidator {


  def validateConfigFileValues(configMap: Map[String, ConfigValue]) : Try[Map[String, ConfigValue]] = {
    configMap values match {
      case DependencyList(list) => {
        list.foreach( dep => {
          dep.artifact.matches("asdas")
        })
      }

    }
  }

  def validateConfigFileKeySet(configMap: Map[String, ConfigValue]): Try[Map[String, ConfigValue]] = {
    if (configMap.keySet.subsetOf(Config.defaultConfig.keySet))
      Success(configMap)
    else
      Failure(new ConfigFileException("Unknown key"))
  }

  def validateConfigFileExists(): Unit = {

  }

  def validateName(): Unit = {

  }

  def validateDependencies = {

  }

  def validateVersion = {

  }
}
