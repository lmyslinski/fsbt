package core

import java.io.PrintWriter

import better.files.File
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.Logger
import compiler.ZincCompiler
import context.ContextUtil
import core.config._
import core.dependencies.DependencyDownloader
import org.slf4j.LoggerFactory
import xsbti.compile.{IncrementalCompiler, ZincCompilerUtil}

import scala.sys.process._

object Fsbt {

  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val compiler: IncrementalCompiler = ZincCompilerUtil.defaultIncrementalCompiler()

  def compile(args: List[String], config: FsbtConfig): Unit = {

    DependencyDownloader.resolveAll(config.dependencies)

    val sourceFiles = config.getScalaSourceFiles
    //::: config.getJavaSourceFiles
    val classPath = config.dependencies.map(_.jarFile)

    logger.debug("Compiling scala...")

    config.target.createIfNotExists(asDirectory = true)

    val cp = new ZincCompiler().compile(classPath, sourceFiles, config.target)

  }

  def main(args: Array[String]): Unit ={
    val config = ConfigBuilder.build("testProject")
    val args = List()
    compile(args, config)
  }

  def run(args: List[String], config: FsbtConfig): Unit = {
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
    val ctx = ContextUtil.identifyContext(config.getTargetClasses)

//    val junit = List("java",  "-cp", config.getTestClassPath) ++ List("org.junit.runner.JUnitCore")
//    val scalaTest = List("java",  "-cp", config.getTestClassPath) ++ List("org.scalatest.tools.Runner", "-R", s"${config.target}", "-o")

//    println(command)

//    val output = command.!
  }

  def clean(config: FsbtConfig): Unit = {
    for (file <- config.target.list) {
      file.delete()
    }
  }

  def nailMain(context: NGContext): Unit = {

    val config = ConfigBuilder.build(context)
    val args = context.getArgs.toList

    if (args.isEmpty) {
      println("Printing info")
    } else
    args.foreach {
      case "compile" => compile(args, config)
      case "test" => {
        compile(args, config)
        test(config)
      }
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

}
