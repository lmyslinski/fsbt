package core

import better.files.File
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.Logger
import context.ContextUtil
import core.config.{ConfigBuilder, ConfigEntry}
import org.slf4j.LoggerFactory

import scala.sys.process._
import scala.util.matching.Regex

object Fsbt {

  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val scalaRegex = new Regex(".scala$")
  // parse only top-level class files, omit nested classes
  val classRegex = new Regex("^[^$]+.class$")

  def recursiveListFiles(path: String, r: Regex): List[File] = {
    val these = File(path).listRecursively
    these.filter(f => r.findFirstIn(f.name).isDefined).toList
  }

  def compile(args: List[String], config: ConfigBuilder): Unit ={
    val filePaths = recursiveListFiles(config.config(ConfigEntry.workingDir).toString, scalaRegex).map( x=> x.path.toAbsolutePath.toString)
    val target = config.config(ConfigEntry.targetDirectory).toString
    File(target).createIfNotExists(asDirectory = true)

    val command = List("scalac") ++ filePaths ++ List("-d", target)
    val output = command.!!
  }

  def run(args: List[String], config: ConfigBuilder): Unit = {

    val targetDir = config.config(ConfigEntry.targetDirectory).toString
    val targetClasses = recursiveListFiles(targetDir, classRegex)
    val ctx = ContextUtil.identifyContext(targetClasses)

    if(ctx.isEmpty){
      println("No context were found")
    }else{
      ctx(0).run(targetDir)
    }
  }

  def nailMain(context: NGContext): Unit ={

    val config = new ConfigBuilder(context)
    val args = context.getArgs.toList

    if (args.isEmpty){
      println("Printing info")
    } else args.head match {
      case "compile" => compile(args, config)
      case "run" => run(args, config)
      case unknown => println ("command not found: " + unknown)
    }
  }

}
