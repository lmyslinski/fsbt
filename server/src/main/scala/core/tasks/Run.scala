package core.tasks

import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import compiler.ScalaLocator
import context.ContextUtil
import core.FsbtUtil
import core.config.{Environment, FsbtConfig}
import core.dependencies.MavenDependencyScope

import scala.sys.process._
import scala.util.matching.Regex

object Run extends Task with LazyLogging {

  private def runtimeClassPath(config: FsbtConfig) = {
    val scalaJarPaths = ScalaLocator.scalaInstance.allJars.map(_.toPath.toAbsolutePath.toString)
    val runtimeDepsPaths = config.dependencies.filter(_.scope == MavenDependencyScope.Runtime).map(_.jarFile.path.toAbsolutePath.toString)
    (scalaJarPaths ++ runtimeDepsPaths ++ Array(config.target.path.toAbsolutePath.toString)).foldLeft("")((dep, res) => dep + Environment.dirSeparator(config.environment) + res)
  }

  // parse only top-level class files, omit nested classes
  val classRegex = new Regex("^[^$]+.class$")

  override def perform(config: FsbtConfig)(implicit ctx: NGContext): Unit = {


    val runCtx = ContextUtil.identifyContext(FsbtUtil.recursiveListFiles(config.target.toString(), classRegex))
    val cp = runtimeClassPath(config)

    if (runCtx.isEmpty) {
      ctx.out.println("No context were found")
    } else {
      val className = runCtx.head.className.get
      val cls = transformClassFormat(className)
      val command = List("java",  "-cp", cp, cls)
      val output = command.lineStream
      output.foreach(ctx.out.println)
    }
  }

  def transformClassFormat(packageString: String) = {
    packageString.replace('/', '.')
  }
}

