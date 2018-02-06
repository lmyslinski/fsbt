package core.execution.tasks

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import compiler.ScalaLocator
import context.ContextUtil
import core.FsbtUtil
import core.config.compile.ExecutionConfig
import core.config.{Environment, FsbtModule}
import core.dependencies.MavenDependencyScope
import core.execution.Task

import scala.sys.process._
import scala.util.matching.Regex

case class Run() extends Task {

  private def runtimeClassPath(config: FsbtModule) = {
    val scalaJarPaths = ScalaLocator.scalaInstance.allJars.map(_.toPath.toAbsolutePath.toString)
    //    val runtimeDepsPaths = FsbtUtil.getNestedDependencies(config, MavenDependencyScope.Runtime).map(_.jarFile.path.toAbsolutePath.toString)
    //    val moduleTargets = config.modules.map(_.target.toJava)
    val target = config.target.path.toAbsolutePath.toString

    //    (moduleTargets ++ scalaJarPaths ++ runtimeDepsPaths :+ target)
    //      .foldRight("")((dep, res) => dep + Environment.pathSeparator(config.environment) + res)
    ""
  }

  override def perform(module: FsbtModule,
                       config: ExecutionConfig,
                       moduleTaskCompleted: FsbtModule => Unit)(implicit ctx: NGContext, logger: Logger): Unit = {

    val runCtx = ContextUtil.identifyContext(FsbtUtil.recursiveListFiles(module.target.toString(), classRegex))
    val cp2 = runtimeClassPath(module)

    val cpElements = config.classpath.targets.map(_.getAbsolutePath) :::
      config.classpath.dependencies.map(_.jarFile.path.toAbsolutePath.toString) :::
    ScalaLocator.scalaInstance.allJars.map(_.toPath.toAbsolutePath.toString).toList

    val cp = cpElements.foldRight("")((dep, res) => dep + Environment.pathSeparator(module.environment) + res)

    if (runCtx.isEmpty) {
      logger.info("No context were found")
    } else {
      val className = runCtx.head.className.get
      val cls = transformClassFormat(className)
      val command = List("java",  "-cp", cp, cls)
      val output = command.lineStream
      output.foreach(logger.info)
    }

  }



  // parse only top-level class files, omit nested classes
  val classRegex = new Regex("^[^$]+.class$")

  def perform(config: FsbtModule)(implicit ctx: NGContext, logger: Logger): Unit = {



  }

  def transformClassFormat(packageString: String): String = {
    packageString.replace('/', '.')
  }
}