package core.tasks

import better.files.File
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import compiler.ZincCompiler
import core.FsbtUtil
import core.cache.FsbtCache
import core.config.FsbtProject
import core.dependencies.DependencyDownloader
import xsbti.compile.CompileResult

import scala.util.matching.Regex

class Compile extends Task with LazyLogging {


  override def perform(config: FsbtProject)(implicit ctx: NGContext): Unit = {

    val compileResults = config.modules.map(compileModule)
    val cr = compileModule(config)
  }

  def compileModule(config: FsbtProject): Option[CompileResult] = {
    DependencyDownloader.resolveAll(config.dependencies)
    config.target.createIfNotExists(asDirectory = true)
    logger.debug(s"Compiling ${config.projectName}...")

    val sourceFiles = getSourceFiles(config.workingDir + "/src").map(_.toJava).toArray
    for (source <- sourceFiles) {
      logger.debug(s"Source: ${source.toPath}")
    }

    val classPath =
      ((config.target.toJava ::
        FsbtUtil.getNestedDependencies(config).map(_.jarFile.toJava)) :::
        config.modules.map(_.target.toJava)).toArray

    try {
      Some(Compile.cp.compile(classPath, sourceFiles, config))
    } catch {
      case ex: Exception =>
        logger.trace("Compilation failed:", ex)
        logger.debug("fuck", ex)
        None
    }
  }

  def getSourceFiles(workingDir: String): List[File] = {
    def getScalaSourceFiles: List[File] = FsbtUtil.recursiveListFiles(workingDir, Compile.scalaRegex)
    def getJavaSourceFiles: List[File] = FsbtUtil.recursiveListFiles(workingDir, Compile.javaRegex)

    getScalaSourceFiles ++ getJavaSourceFiles
  }

}

object Compile {
  val scalaRegex = new Regex(".scala$")
  val javaRegex = new Regex(".java$")
  val cp = new ZincCompiler()
  FsbtCache.loadCache()
}