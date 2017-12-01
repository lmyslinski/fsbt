package core.config

object ConfigEntry extends Enumeration{
	val name = Value("name")
	val version = Value("version")
	val sourceDirectory = Value("sourceDir")
	val targetDirectory = Value("targetDir")
	val dependencyList = Value("libraryDependencies")
	val unmanagedJar = Value("unmanagedJar")
	val workingDir = Value("workDir")
	val modules = Value("submodules")
}