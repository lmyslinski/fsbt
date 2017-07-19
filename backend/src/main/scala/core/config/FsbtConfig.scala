package core.config

import better.files.File
import core.dependencies.MavenDependency

import scala.util.matching.Regex

/**
  * Created by humblehound on 21.07.17.
  */
case class FsbtConfig(topLevelDependencies: List[MavenDependency], target: File, workingDir: String){

  val scalaRegex = new Regex(".scala$")
  val javaRegex = new Regex(".java$")
  // parse only top-level class files, omit nested classes
  val classRegex = new Regex("^[^$]+.class$")

  def recursiveListFiles(path: String, r: Regex): List[File] = {
    val these = File(path).listRecursively
    these.filter(f => r.findFirstIn(f.name).isDefined).toList
  }

  def getScalaSourceFiles = recursiveListFiles(workingDir, scalaRegex).map(x => x.path.toAbsolutePath.toString)
  def getJavaSourceFiles = recursiveListFiles(workingDir, javaRegex).map(x => x.path.toAbsolutePath.toString)
//  def getClasspath = topLevelDependencies.foldRight("")((dep, res) => dep.jarFile.path.toAbsolutePath.toString + ":" + res)
  def getTargetClasses = recursiveListFiles(target.toString(), classRegex)
  def dependencies = topLevelDependencies.flatMap(_.resolve(true))

  lazy val classPath = dependencies.foldRight("")((dep, res) => dep.jarFile.path.toAbsolutePath.toString + ":" + res)

}
