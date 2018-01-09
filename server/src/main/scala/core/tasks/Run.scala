package core.tasks

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import compiler.ScalaLocator
import context.ContextUtil
import core.FsbtUtil
import core.config.{Environment, FsbtProject}
import core.dependencies.MavenDependencyScope

import scala.sys.process._
import scala.util.matching.Regex

class Run extends Task {

  private def runtimeClassPath(config: FsbtProject) = {
    val scalaJarPaths = ScalaLocator.scalaInstance.allJars.map(_.toPath.toAbsolutePath.toString)
    val runtimeDepsPaths = FsbtUtil.getNestedDependencies(config, MavenDependencyScope.Runtime).map(_.jarFile.path.toAbsolutePath.toString)
    val moduleTargets = config.modules.map(_.target.toJava)
    val target = config.target.path.toAbsolutePath.toString

    (moduleTargets ++ scalaJarPaths ++ runtimeDepsPaths :+ target)
      .foldRight("")((dep, res) => dep + Environment.pathSeparator(config.environment) + res)
  }

  // parse only top-level class files, omit nested classes
  val classRegex = new Regex("^[^$]+.class$")

  override def perform(config: FsbtProject)(implicit ctx: NGContext, logger: Logger): Unit = {


    val runCtx = ContextUtil.identifyContext(FsbtUtil.recursiveListFiles(config.target.toString(), classRegex))
    val cp = runtimeClassPath(config)

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

  def transformClassFormat(packageString: String): String = {
    packageString.replace('/', '.')
  }
}

object Run{

}