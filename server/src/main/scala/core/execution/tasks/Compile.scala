package core.execution.tasks

import better.files.File
import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import compiler.ZincCompiler
import core.FsbtUtil
import core.cache.FsbtCache
import core.config.FsbtModule
import core.config.compile.ExecutionConfig
import core.dependencies.{DependencyDownloader, DependencyResolver}
import core.execution.Task
import util.LazyNailLogging

import scala.util.matching.Regex

case class Compile() extends Task with LazyNailLogging {

  override def perform(module: FsbtModule, config: ExecutionConfig, moduleTaskCompleted: FsbtModule => Unit)(implicit ctx: NGContext, ļlogger: Logger): Unit = {
    compileModule(module, config)
    moduleTaskCompleted.apply(module)
  }

  def compileModule(module: FsbtModule, config: ExecutionConfig)(implicit logger: Logger): Unit = {

    val deps = DependencyResolver.resolveAll(config.classpath.dependencies)
    DependencyDownloader.resolveAll(deps)
    module.target.createIfNotExists(asDirectory = true)

    logger.info(s"[${module.projectName}] Compiling")

    val srcRoot = File(module.workingDir + "/src")
    if (srcRoot.exists) {
      val sourceFiles = getSourceFiles(srcRoot.pathAsString).map(_.toJava).toArray

      val classPath = (module.target.toJava :: deps.map(_.jarFile.toJava) ::: config.classpath.targets).toArray
        Compile.cp.compile(classPath, sourceFiles, module)
        logger.info(s"[${module.projectName}] compilation completed")

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