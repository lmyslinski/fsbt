package core.tasks

import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import compiler.ScalaLocator
import context.ContextUtil
import core.config.FsbtConfig
import core.dependencies.MavenDependencyScope

import scala.sys.process._

object Run extends Task with LazyLogging {

  private def runtimeClassPath(config: FsbtConfig) = {
    val scalaJarPaths = ScalaLocator.scalaInstance.allJars.map(_.toPath.toAbsolutePath.toString)
    val runtimeDepsPaths = config.dependencies.filter(_.scope == MavenDependencyScope.Runtime).map(_.jarFile.path.toAbsolutePath.toString)
    (scalaJarPaths ++ runtimeDepsPaths ++ Array(config.target.path.toAbsolutePath.toString)).foldLeft("")((dep, res) => dep + config.getPathSeparator() + res)
  }

  override def perform(config: FsbtConfig)(implicit ctx: NGContext): Unit = {

    logger.debug("Running main class")

    val ctx = ContextUtil.identifyContext(config.getTargetClasses)
    val cp = runtimeClassPath(config)

    println(ctx)
    if (ctx.isEmpty) {
      println("No context were found")
    } else {
      val className = ctx.head.className.get
      val cls = transformClassFormat(className)
      val command = List("java",  "-cp", cp, cls)
      val output = command.lineStream
      output.foreach(println)
    }
  }

  def transformClassFormat(packageString: String) = {
    packageString.replace('/', '.')
  }
}

