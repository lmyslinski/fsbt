package core

import better.files.File
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.Logger
import context.ContextUtil
import core.config.{ConfigBuilder, ConfigDSL, ConfigEntry, Dependency}
import core.dependencies.MavenDependency
import org.slf4j.LoggerFactory
import sbt.inc.ZincUtils
import sbt.internal.inc.ScalaInstance
import xsbti.compile.{DependencyChanges, Output}

import scala.sys.process._
import scala.util.matching.Regex

object Fsbt {

  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val scalaRegex = new Regex(".scala$")
  val javaRegex = new Regex(".java$")
  // parse only top-level class files, omit nested classes
  val classRegex = new Regex("^[^$]+.class$")

  def recursiveListFiles(path: String, r: Regex): List[File] = {
    val these = File(path).listRecursively
    these.filter(f => r.findFirstIn(f.name).isDefined).toList
  }

  def compile(args: List[String], config: ConfigBuilder): Unit ={
    val scalaFilePaths = recursiveListFiles(config.config(ConfigEntry.workingDir).toString, scalaRegex).map( x=> x.path.toAbsolutePath.toString)
    val javaFilePaths = recursiveListFiles(config.config(ConfigEntry.workingDir).toString, javaRegex).map( x=> x.path.toAbsolutePath.toString)

    val target = config.config(ConfigEntry.targetDirectory).toString
    File(target).createIfNotExists(asDirectory = true)

//    val libJar = new File("/home/humblehound/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.11.8.jar").toJava
//    val compilerJar = new File("/home/humblehound/.ivy2/cache/org.scala-lang/scala-compiler/jars/scala-compiler-2.11.8.jar").toJava
//    val scalaInstance = new ScalaInstance("2.11.8", ClassLoader.getSystemClassLoader, libJar, compilerJar, Array(), None)
//    val bridgeJar = new File("/home/humblehound/.ivy2/cache/org.scala-sbt/zinc-compile-core_2.11/jars/zinc-compile-core_2.11-1.0.0-X10.jar").toJava
//
//    val compiler = ZincUtils.scalaCompiler(scalaInstance, bridgeJar)

    for(file <- scalaFilePaths){
      println(s"Found source file: $file")
    }

    for(file <- javaFilePaths){
      println(s"Found source file: $file")
    }

//    val dependencies = config.config(ConfigEntry.dependencyList).asInstanceOf[List[Dependency]].map(p => MavenDependency(p).getJarFile.path.toAbsolutePath.toString)
    val dependencies = config.config(ConfigEntry.dependencyList).asInstanceOf[List[Dependency]].foldRight("")((dep, res) => MavenDependency(dep).getJarFile.path.toAbsolutePath.toString + ";" + res) + "."

    val compileScala = List("scalac") ++ scalaFilePaths ++ List("-cp", dependencies) ++ List("-d", target)
    println(compileScala)
    val scalaOutput = compileScala.!!
    val compileJava = List("javac") ++ javaFilePaths ++ List("-cp", dependencies) ++ List("-d", target)
    println(compileJava)
    val output = compileJava.!!
    println(output)
    ()}

  def run(args: List[String], config: ConfigBuilder): Unit = {

    val targetDir = config.config(ConfigEntry.targetDirectory).toString

    val deps = config.config(ConfigEntry.dependencyList).asInstanceOf[List[Dependency]]
    for (dep1 <- deps){
      val d = MavenDependency(dep1)
      println(d.baseUri)
      d.downloadDependencies
    }

    ConfigDSL.parseConfigFile(config.configFilePath)
    val targetClasses = recursiveListFiles(targetDir, classRegex)
    val ctx = ContextUtil.identifyContext(targetClasses)

    if(ctx.isEmpty){
      println("No context were found")
    }else{
      ctx(0).run(targetDir)
    }
  }

  def clean(config: ConfigBuilder) = {
    val target = File(config.config(ConfigEntry.targetDirectory).asInstanceOf[String])
    for(file <- target.list){
      println(file.path)
      file.delete()
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
      case "clean" => clean(config)
      case unknown => println ("command not found: " + unknown)
    }
  }

}
