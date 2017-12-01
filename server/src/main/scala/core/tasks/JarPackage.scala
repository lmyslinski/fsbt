package core.tasks

import java.io.PrintWriter

import better.files.File
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import core.FsbtUtil
import core.config.FsbtConfig

import scala.sys.process.Process
import scala.util.matching.Regex

object JarPackage extends Task with LazyLogging{
  override def perform(config: FsbtConfig)(implicit ctx: NGContext): Unit = {
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

    val jarCmd = List("jar", "cfm", config.target.toString + s"/${config.projectName}.jar", manifestFile.getPath) ::: classes
    Process(jarCmd, config.target.toJava).!
  }
}
