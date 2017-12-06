package core.tasks

import better.files.File
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import compiler.ZincCompiler
import core.cache.FsbtCache
import core.config.FsbtProject
import core.dependencies.{DependencyDownloader, DependencyResolver, MavenDependency}
import core.FsbtUtil

import scala.util.matching.Regex

class Compile extends Task with LazyLogging {


  override def perform(config: FsbtProject)(implicit ctx: NGContext): Unit = {

    new DependencyResolver(config.dependencies).resolveAll()
    DependencyDownloader.resolveAll(config.dependencies)

    config.target.createIfNotExists(asDirectory = true)
    logger.debug("Compiling...")
    val sourceFiles = getSourceFiles(config.workingDir).map(_.toJava).toArray
    val classPath = config.dependencies.map(_.jarFile.toJava).toArray :+ config.target.toJava
    Compile.cp.compile(classPath, sourceFiles, config)
  }

  def transformClassFormat(packageString: String) = {
    packageString.replace('/', '.')
  }

  def getSourceFiles(workingDir: String) = {
    def getScalaSourceFiles: List[File] = FsbtUtil.recursiveListFiles(workingDir, Compile.scalaRegex)
    def getJavaSourceFiles: List[File] = FsbtUtil.recursiveListFiles(workingDir, Compile.javaRegex)
    getScalaSourceFiles
    //++ getJavaSourceFiles
  }

}

object Compile{
  val scalaRegex = new Regex(".scala$")
  val javaRegex = new Regex(".java$")
  val cp = new ZincCompiler()
  FsbtCache.loadCache()
}