package core.config

import java.nio.file.{Files, Paths}

import scala.util.{Failure, Success, Try}

object ConfigValidator {
	def validateConfigFileKeySet(configMap: Map[ConfigEntry.Value, ConfigValue]) = {
    Success(configMap)
  }

	// how do we validate the values?
  def validateConfigFileValues(configMap: Map[ConfigEntry.Value, ConfigValue]) : Try[Map[ConfigEntry.Value, ConfigValue]] = {
    Success(configMap)
  }

  def validateConfigFileExists(configFileUri: String): Try[String] = {
      if(Files.exists(Paths.get(configFileUri))) {
        Success(configFileUri)
      }
      else {
        Failure(new RuntimeException("Fsbt config file not found"))
      }
  }
}
