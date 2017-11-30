package core

import java.io.PrintWriter
import java.util.function.Supplier

import better.files.File
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import core.config._
import core.tasks.{Compile, Run}

import scala.sys.process._

object Fsbt extends LazyLogging{


  def main(args: Array[String]): Unit ={
    println("Not running as nailgun!")
  }

  def createJar(args: List[String], config: FsbtConfig): Unit = {
    val mf = File(config.target.toString() + "/META-INF/MANIFEST.MF")
    mf.parent.createIfNotExists(asDirectory = true, createParents = true)
    val manifestFile = mf.toJava
    val pw = new PrintWriter(manifestFile)
    pw.write("Manifest-Version: 1.0\n")
    pw.write("Main-Class: main.root.Main\n")
    pw.write("\n")
    pw.close()
    val classes = config.getAllTargetClasses.map(_.pathAsString.split(config.target.pathAsString + '/')(1))
    val jarCmd = List("jar", "cfm", config.target.toString + s"/${config.projectName}.jar", manifestFile.getPath) ::: classes
    Process(jarCmd, config.target.toJava).!
  }

  def test(config: FsbtConfig): Unit = {
    val scalaTest = List("java",  "-cp", config.getClasspath) ++ List("org.scalatest.tools.Runner", "-R", s"${config.target}", "-o")
    println(scalaTest)
    val output = scalaTest.!
  }

  def clean(config: FsbtConfig): Unit = {
    for (file <- config.target.list) {
      file.delete()
    }
  }

  def nailMain(context: NGContext): Unit = {
    val config = ConfigBuilder.build(context)
    val args = context.getArgs.toList
    implicit val ctx: NGContext = context

    if (args.isEmpty) {
      context.out.println("fsbt@0.0.1")
    } else
    args.foreach {
      case "stop" =>
        context.getNGServer.shutdown(true)
      case "compile" =>
        Compile.perform(config)
      case "test" =>
        Compile.perform(config)
        test(config)
      case "run" =>
        Compile.perform(config)
        Run.perform(config)
      case "package" =>
        Compile.perform(config)
        createJar(args, config)
      case "clean" => clean(config)
      case unknown => context.out.println("command not found: " + unknown)
    }
  }

  def executeTask(f: (List[String], FsbtConfig) => Unit, args: List[String], config: FsbtConfig): Unit ={
    try{
      f(args, config)
    }catch{
      case ex: Exception => logger.debug("Oops")
    }
  }

    def getLogger: xsbti.Logger = {
      new xsbti.Logger {

        override def debug(msg: Supplier[String]): Unit = logger.debug(msg.get())

        override def error(msg: Supplier[String]): Unit = logger.error(msg.get())

        override def warn(msg: Supplier[String]): Unit = logger.warn(msg.get())

        override def trace(exception: Supplier[Throwable]): Unit = logger.trace("", exception.get())

        override def info(msg: Supplier[String]): Unit = logger.info(msg.get())
      }
    }


  }
