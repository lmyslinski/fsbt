package core.config

/**
  * Created by Admin on 30.03.2017.
  */

object ConfigEntry extends Enumeration{
	val name = Value("name")
	val version = Value("version")
	val sourceDirectory = Value("sourceDir")
	val targetDirectory = Value("targetDir")
	val dependencyList = Value("libraryDependencies")
}