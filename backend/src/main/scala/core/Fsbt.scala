package core

import java.io.PrintWriter
import java.{io, util}
import java.util.Optional
import java.util.function.Supplier

import better.files.File
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.Logger
import compiler.ZincCompiler
import compiler.pants.{AnalysisMap, AnalysisOptions, ConsoleOptions, InputUtils, SbtJars, ScalaLocation}
import context.ContextUtil
import core.config._
import core.dependencies.DependencyDownloader
import org.slf4j.LoggerFactory
import sbt.internal.inc.ZincUtil
import sbt.util.Level
import xsbti.compile.{PreviousResult, ZincCompilerUtil}

import scala.sys.process._

object Fsbt {

  val logger = Logger(LoggerFactory.getLogger(this.getClass))

//  val compiler: IncrementalCompiler = ZincCompilerUtil.defaultIncrementalCompiler()

  def compile(args: List[String], config: FsbtConfig): Unit = {

    DependencyDownloader.resolveAll(config.dependencies)

    val sourceFiles = config.getScalaSourceFiles
    //::: config.getJavaSourceFiles
    val classPath = config.dependencies.map(_.jarFile)

    logger.debug("Compiling scala...")

    config.target.createIfNotExists(asDirectory = true)

    val cp = new ZincCompiler().compile(classPath, sourceFiles, config.target)
  }

//  val compilerJar = new java.io.File("C:\\Users\\lukaszmy\\.ivy2\\cache\\org.scala-sbt\\compiler-bridge_2.12\\jars\\compiler-bridge_2.12-1.0.0.jar")
//
//  val libJar = new java.io.File("C:\\Users\\lukaszmy\\.ivy2\\cache\\org.scala-lang\\scala-library\\jars\\scala-library-2.12.3.jar")
//
//  val reflectJar = new java.io.File("C:\\Users\\lukaszmy\\.ivy2\\cache\\org.scala-lang\\scala-reflect\\jars\\scala-reflect-2.12.3.jar")

//  val allJars = Array(libJar, compilerJar, reflectJar)

  val zincCache = new java.io.File("/home/humblehound/Dev/fsbt/testProject/target")

  val zincCacheFile = new java.io.File("/home/humblehound/Dev/fsbt/testProject/target/zincCache")

  val scalaDir = new java.io.File("C:\\Program Files (x86)\\scala")

  def compilePants(args: List[String], config: FsbtConfig): Unit = {

    DependencyDownloader.resolveAll(config.dependencies)

    def urlses(cl: ClassLoader): Array[java.net.URL] = cl match {
      case null => Array()
      case u: java.net.URLClassLoader => u.getURLs() ++ urlses(cl.getParent)
      case _ => urlses(cl.getParent)
    }

    val  urls = urlses(getClass.getClassLoader)
    println(urls.filterNot(_.toString.contains("ivy")).mkString("\n"))

    val sourceFiles = config.getScalaSourceFiles
    //::: config.getJavaSourceFiles
    val classPath = config.dependencies.map(_.jarFile)

//    logger.debug("Compiling scala...")

    config.target.createIfNotExists(asDirectory = true)

    val scalaPath: java.io.File = new java.io.File("C:\\Program Files (x86)\\scala\\lib")

    val zincLogger = getLogger

    val sources = config.getScalaSourceFiles.map(_.toJava).toSeq

    val sbtJars = SbtJars.apply(
      Some(new java.io.File("/home/humblehound/Dev/fsbt/backend/lib/scala-compiler-2.12.4.jar")),
      Some(new java.io.File("/home/humblehound/Dev/fsbt/backend/lib/compiler-bridge_2.12-1.0.0.jar")))

    val scalaLocation = ScalaLocation.fromHome(new java.io.File("/opt/scala-2.12.4"))

//    val scalaLocation = ScalaLocation.create(scalaPath, new util.ArrayList[java.io.File](){scalaPath}, compilerJar, libJar, new util.ArrayList[java.io.File](){reflectJar} )

    val amap = AnalysisMap.create(AnalysisOptions(Option(zincCacheFile)))

    val settings = compiler.pants.Settings(
      consoleLog = ConsoleOptions(Level.Debug),
      _sources = sources,
      _classesDirectory = Option(zincCache),
      sbt = sbtJars,
      _zincCacheDir = Option.apply(zincCache),
      analysis = AnalysisOptions(Option(zincCacheFile)),
      scala = scalaLocation)


    val pr = PreviousResult.create(Optional.empty(), Optional.empty())

    val input = InputUtils.create(settings, amap, pr, zincLogger)

    val cp = new sbt.internal.inc.IncrementalCompilerImpl()
    val cr = cp.compile(input, zincLogger)
    logger.debug(cr.toString)

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
      case "compile" => compilePants(args, config)
      case "test" => {
        compile(args, config)
        test(config)
      }
      case "run" =>
        compilePants(args, config)
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
