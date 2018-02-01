package core.tasks

import better.files.File
import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import compiler.ZincCompiler
import core.FsbtUtil
import core.cache.FsbtCache
import core.config.FsbtModule
import core.config.compile.ExecutionConfig
import core.dependencies.{DependencyDownloader, DependencyResolver, MavenDependencyScope}
import util.LazyNailLogging
import xsbti.compile.CompileResult

import scala.util.matching.Regex

class Compile extends Task with LazyNailLogging {

  override def perform(module: FsbtModule, moduleTaskCompleted: FsbtModule => Unit)(implicit ctx: NGContext, logger: Logger): Unit = {
    println(s"Performing with ${module.projectName}")
    compileModule(module)
    moduleTaskCompleted.apply(module)
  }

  private def getDependencies(config: FsbtModule) =
    config.dependencies
      .filter(x => x.scope == MavenDependencyScope.Test || x.scope == MavenDependencyScope.Compile)


  def compileModule(config: FsbtModule)(implicit logger: Logger): Option[CompileResult] = {

    val deps = DependencyResolver.resolveAll(config.dependencies)
    DependencyDownloader.resolveAll(deps)
    config.target.createIfNotExists(asDirectory = true)

    val configCopy = config.copy(dependencies = deps)


    val srcRoot = File(configCopy.workingDir + "/src")
    if (srcRoot.exists) {
      val sourceFiles = getSourceFiles(srcRoot.pathAsString).map(_.toJava).toArray

      val classPath =
        (configCopy.target.toJava ::
          getDependencies(configCopy).map(_.jarFile.toJava)).toArray

      try {

        Compile.cp.compile(classPath, sourceFiles, configCopy)
      } catch {
        case ex: Exception =>
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