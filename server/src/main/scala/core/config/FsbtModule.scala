package core.config

import better.files.File
import core.config.FsbtModule.{FsbtProjectRef, Variables}
import core.dependencies.MavenDependency

case class FsbtModule(
                        dependencies: List[MavenDependency],
                        workingDir: String,
                        target: File,
                        projectName: String,
                        environment: Environment.Value,
                        variables: Variables,
                        modules: List[FsbtProjectRef],
                        dependsOn: List[FsbtProjectRef],
                        isRootProject: Boolean = false
                      ) {
  override def equals(that: Any): Boolean =
    that match {
      case that: FsbtModule => that.canEqual(this) && this.hashCode == that.hashCode
      case _ => false
    }

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result
    result = prime * result + projectName.hashCode
    result
  }

  override def toString: String = projectName
}

object FsbtModule {

  val fsbtPath = System.getProperty("user.home") + "/.fsbt"
  val fsbtCache = s"$fsbtPath/cache"
  val zincCache = s"$fsbtPath/cache/compileCache"
  val scalaVersion = "_2.12"
  type Variables = Map[String, String]
  type FsbtProjectRef = String
}


