package core.tasks

import java.io.PrintWriter

import better.files.File
import ch.qos.logback.classic
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import core.FsbtUtil
import core.config.FsbtModule
import org.slf4j.Logger

import scala.sys.process.Process
import scala.util.matching.Regex

class JarPackage extends Task with LazyLogging{
  def perform(config: FsbtModule)(implicit ctx: NGContext, logger: classic.Logger): Unit = {
    logger.debug(s"Packaging ${config.projectName}...")
    val mf = File(config.target.toString() + "/META-INF/MANIFEST.MF")
    mf.parent.createIfNotExists(asDirectory = true, createParents = true)
    val manifestFile = mf.toJava
    val pw = new PrintWriter(manifestFile)
    pw.write("Manifest-Version: 1.0\n")
    pw.write("Main-Class: main.root.Main\n")
    pw.write("\n")
    pw.close()
    val classes =
      FsbtUtil.recursiveListFiles(s"${config.target}", new Regex(".*\\.class"))
        .map(_.pathAsString.split(config.target.pathAsString + '/')(1))

    val name = config.target.toString + s"/${config.projectName}.jar"

    val jarCmd = List("jar", "cfm", name, manifestFile.getPath) ::: classes
    Process(jarCmd, config.target.toJava).!
    logger.debug(s"Generated ${config.projectName}.jar at ${config.target}")

  }

  override def perform(module: FsbtModule, moduleTaskCompleted: FsbtModule => Unit)(implicit ctx: NGContext, logger: classic.Logger): Unit = {

  }
}

object JarPackage{

}