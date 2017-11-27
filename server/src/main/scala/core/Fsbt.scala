package core

import java.io.PrintWriter
import java.util.function.Supplier

import better.files.File
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import compiler.ZincCompiler
import context.ContextUtil
import core.config._
import core.dependencies.DependencyDownloader

import scala.sys.process._

object Fsbt extends LazyLogging{

  val cp = new ZincCompiler()

  def compile(args: List[String], config: FsbtConfig): Unit = {

    DependencyDownloader.resolveAll(config.dependencies)
    config.target.createIfNotExists(asDirectory = true)
    logger.debug("Compiling...")
    val cr = cp.compile(config)
    FsbtConfig.crCache = Some(cr)
  }

  def main(args: Array[String]): Unit ={
    val config = ConfigBuilder.build("testProject")
    val args = List()
    compile(args, config)
  }

  def run(args: List[String], config: FsbtConfig): Unit = {
    logger.debug("Running main class")
    val ctx = ContextUtil.identifyContext(config.getTargetClasses)
    println(ctx)
    if (ctx.isEmpty) {
      println("No context were found")
    } else {
      ctx.head.run(config.target)
    }
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
    println("Running nail main class")
    val config = ConfigBuilder.build(context)
    val args = context.getArgs.toList

    if (args.isEmpty) {
      logger.debug("Printing info")
    } else
    args.foreach {
      case "stop" =>
        logger.debug("Exiting")
        context.getNGServer.shutdown(true)
      case "compile" =>
        try{
          compile(args, config)
        }catch {
          case ex: Exception => logger.debug("Oops")
        }
      case "test" =>
        compile(args, config)
        test(config)
      case "run" =>
        compile(args, config)
        run(args, config)
      case "package" =>
        compile(args, config)
        createJar(args, config)
      case "clean" => clean(config)
      case unknown => println("command not found: " + unknown)
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
