package core.config

import better.files.File
import core.config.FsbtProject.Variables
import core.dependencies.MavenDependency

case class FsbtProject(
                       dependencies: List[MavenDependency],
                       workingDir: String,
                       target: File,
                       projectName: String,
                       environment: Environment.Value,
                       variables: Variables,
                       modules: List[FsbtModule])

object FsbtProject {

  val fsbtPath = System.getProperty("user.home") + "/.fsbt"
  val fsbtCache = s"$fsbtPath/cache"
  val zincCache = s"$fsbtPath/cache/compileCache"
  val scalaVersion = "_2.12"
  type Variables = Map[String, String]
}


