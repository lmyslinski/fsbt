package core.tasks

import better.files.File
import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import compiler.ZincCompiler
import core.FsbtUtil
import core.cache.FsbtCache
import core.config.FsbtProject
import core.dependencies.{DependencyDownloader, MavenDependencyScope}
import util.LazyNailLogging
import xsbti.compile.CompileResult

import scala.util.matching.Regex

class Compile extends Task with LazyNailLogging {

  override def perform(config: FsbtProject)(implicit ctx: NGContext, logger: Logger): Unit = {

    val compileResults = config.modules.map(compileModule)
    val cr = compileModule(config)

    logger.debug("Compile task complete")
  }

  private def getDependencies(config: FsbtProject) =
    config.dependencies
      .filter(x => x.scope == MavenDependencyScope.Test || x.scope == MavenDependencyScope.Compile)
//      .foldRight("")((dep, res) =>
//        dep.jarFile.path.toAbsolutePath.toString +
//          Environment.pathSeparator(config.environment) +
//          res) + "."


  def compileModule(config: FsbtProject)(implicit logger: Logger): Option[CompileResult] = {
    DependencyDownloader.resolveAll(config.dependencies)
    config.target.createIfNotExists(asDirectory = true)
//    logger.debug(s"Compiling ${config.projectName}...")


    val srcRoot = File(config.workingDir + "/src")
    if (srcRoot.exists) {
      val sourceFiles = getSourceFiles(srcRoot.pathAsString).map(_.toJava).toArray
//      for (source <- sourceFiles) {
//        logger.debug(s"Source: ${source.toPath}")
//      }

      val classPath =
        ((config.target.toJava ::
          getDependencies(config).map(_.jarFile.toJava)) :::
          config.modules.map(_.target.toJava)).toArray

      try {
        Compile.cp.compile(classPath, sourceFiles, config)
      } catch {
        case ex: Exception =>
//          logger.debug("Compilation failed:", ex)
          None
      }
    }else{
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