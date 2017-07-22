package core

import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.Logger
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

//    val deps = config.dependencies

    DependencyDownloader.resolveAll(config.dependencies)


//    val classPath = deps.foldRight("")((dep, res) => dep.jarFile.path.toAbsolutePath.toString + ":" + res)

    logger.debug("Classpath: ")

    config.dependencies.sortWith((a, b) => a.descriptor.compare(b.descriptor) < 0).foreach(f => logger.debug(f.toString))
//    config.dependencies.foreach(p => logger.debug(p.toString))

//    deps.sortWith((a, b) => s"${a.groupIdParsed}/${a.artifactIdParsed}/${a.versionParsed}"
//      .compare(s"${b.groupIdParsed}/${b.artifactIdParsed}/${b.versionParsed}") < 0)
//      .foreach(f => logger.debug(f.jarFile.path.toString))

    val scalaSourceFiles = config.getScalaSourceFiles
    val javaSourceFiles = config.getJavaSourceFiles

    config.target.createIfNotExists(asDirectory = true)

    logger.debug("Compiling scala...")
    val compileScala = List("scalac", "-cp", config.getClasspath) ++ scalaSourceFiles ++ List("-d", config.target.toJava.getAbsolutePath)


    val t0 = System.nanoTime()
    val scalaOutput = compileScala.!!
    val t1 = System.nanoTime()
    logger.debug(s"Elapsed: ${(t1 - t0)/1000000} ms")

    logger.debug("Compiling java...")
    val compileJava = List("javac", "-cp", config.getClasspath) ++ javaSourceFiles ++ List("-d", config.target.toJava.getAbsolutePath)
    val t2 = System.nanoTime()
    val output = compileJava.!!
    val t3 = System.nanoTime()
    logger.debug(s"Elapsed: ${(t3 - t2)/1000000} ms")
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

  def test(config: FsbtConfig): Unit = {

    val targetClasses = config.getTargetClasses.map(_.toString())
    val command = List("java",  "-cp", "/home/humblehound/Dev/fsbt/testProject/target/test/java/TestJunit.class") ++ List("org.junit.runner.JUnitCore", "test.java.TestJunit")
    println(command)
    val output = command.lineStream
    output.foreach(println)
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
      case "test" => test(config)
      case "run" =>
        compile(args, config)
        run(args, config)
      case "clean" => clean(config)
      case unknown => println("command not found: " + unknown)
    }
  }

}
