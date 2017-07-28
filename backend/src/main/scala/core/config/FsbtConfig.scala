package core.config

import better.files.File
import core.dependencies.{DependencyResolver, MavenDependency}

import scala.util.matching.Regex

/**
  * Created by humblehound on 21.07.17.
  */
class FsbtConfig(rawDependencies: List[MavenDependency], val target: File, workingDir: String){

  val scalaRegex = new Regex(".scala$")
  val javaRegex = new Regex(".java$")
  // parse only top-level class files, omit nested classes
  val classRegex = new Regex("^[^$]+.class$")

  def recursiveListFiles(path: String, r: Regex): List[File] = {
    val these = File(path).listRecursively
    these.filter(f => r.findFirstIn(f.name).isDefined).toList
  }

  val scalaSourceDir = workingDir + "/src/main/scala"
  val javaSourceDir = workingDir + "/src/main/java"
  val scalaTestSourceDir = workingDir + "/src/test/scala"
  val javaTestSourceDir = workingDir + "/src/test/java"

  def getScalaSourceFiles: List[File] = recursiveListFiles(workingDir, scalaRegex)
  def getJavaSourceFiles: List[File] = recursiveListFiles(workingDir, javaRegex)


  def getClasspath = dependencies.foldRight("")((dep, res) => dep.jarFile.path.toAbsolutePath.toString + ":" + res) + "."
  def getTestClassPath = dependencies.foldRight("")((dep, res) => dep.jarFile.path.toAbsolutePath.toString + ":" + res) + s"$target"

  def scalaTarget = File(s"${target}/scala")
  def javaTarget = File(s"${target}/java")

  def getTargetClasses = recursiveListFiles(target.toString(), classRegex)
  def getTestClasses = recursiveListFiles(s"$target", classRegex)

  val dependencies: List[MavenDependency] = new DependencyResolver(rawDependencies).resolveAll()

}

object FsbtConfig {
  val fsbtPath = System.getProperty("user.home") + "/.fsbt"
  val fsbtCache = s"$fsbtPath/cache"
  val scalaVersion = "_2.12"
}


