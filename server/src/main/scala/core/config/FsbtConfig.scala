package core.config

import better.files.File
import core.dependencies.MavenDependency

case class FsbtConfig(
                       dependencies: List[MavenDependency],
                       target: File,
                       workingDir: String,
                       projectName: String,
                       environment: Environment.Value)

object FsbtConfig {
  val fsbtPath = System.getProperty("user.home") + "/.fsbt"
  val fsbtCache = s"$fsbtPath/cache"
  val zincCache = s"$fsbtPath/cache/compileCache"
  val scalaVersion = "_2.12"
}


