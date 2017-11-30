package core.tasks

import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import compiler.ZincCompiler
import core.cache.FsbtCache
import core.config.FsbtConfig
import core.dependencies.DependencyDownloader

object Compile extends Task with LazyLogging {

  val cp = new ZincCompiler()
  FsbtCache.loadCache()

  override def perform(config: FsbtConfig)(implicit ctx: NGContext): Unit = {
    DependencyDownloader.resolveAll(config.dependencies)
    config.target.createIfNotExists(asDirectory = true)
    logger.debug("Compiling...")

    val sourceFiles = config.getScalaSourceFiles.map(_.toJava).toArray
    val classPath = config.dependencies.map(_.jarFile.toJava).toArray :+ config.target.toJava
    cp.compile(classPath, sourceFiles, config)
  }

  def transformClassFormat(packageString: String) = {
    packageString.replace('/', '.')
  }

}
