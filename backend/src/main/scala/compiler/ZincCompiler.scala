package compiler

import java.io.File
import java.lang.{Boolean => JBoolean}
import java.net.URLClassLoader
import java.util.Optional
import java.util.function.{Supplier, Function => JFunction}

import better.files
import com.typesafe.scalalogging.Logger
import compiler.pants.{IncOptions, Util}
import core.config.FsbtConfig
import org.slf4j.LoggerFactory
import sbt.internal.inc.javac.{JavaCompiler, JavaTools, Javadoc}
import sbt.internal.inc.{AnalyzingCompiler, ScalaInstance, ZincUtil}
import sbt.io.Path
import xsbti._
import xsbti.compile._

/**
  * Created by humblehound on 24.07.17.
  */
class ZincCompiler {


  private val incOptions: IncOptions = IncOptions()

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  private val zincLogger = new xsbti.Logger {

    override def debug(msg: Supplier[String]): Unit = logger.debug(msg.get())

    override def error(msg: Supplier[String]): Unit = logger.error(msg.get())

    override def warn(msg: Supplier[String]): Unit = logger.warn(msg.get())

    override def trace(exception: Supplier[Throwable]): Unit = logger.trace("trace message", exception.get())

    override def info(msg: Supplier[String]): Unit = logger.info(msg.get())
  }




  def compile(classPath: List[files.File], sourceFiles: List[files.File], target: files.File) = {
    val cp = ZincCompilerUtil.defaultIncrementalCompiler()

    val compilers = Compilers.create(compiler, javaTools)

    val setup = Setup.create(getPerClasspathEntryLookup, false, new File(FsbtConfig.zincCache), CompilerCache.fresh , incOptions.options(zincLogger), reporter, Optional.empty(), Array.empty)

    val previousResult = PreviousResult.create(Optional.empty(), Optional.empty())

    val inputs = Inputs.create(compilers,
      CompileOptions.create().withClasspath(classPath.map(_.toJava).toArray).withClassesDirectory(target.toJava).withSources(sourceFiles.map(_.toJava).toArray),
      setup,
      previousResult)
    val cr = cp.compile(inputs, zincLogger)
    logger.debug(cr.toString)
  }

  def getSourcePositionMapper = new Function[Position, Position]() {
    override def apply(a: Position): Position = a
  }





  def getPerClasspathEntryLookup = new PerClasspathEntryLookup {

    override def definesClass(classpathEntry: File): DefinesClass = new DefinesClass {
      override def apply(className: String): Boolean = {
        logger.debug(s"checking $className on classpath")
        true
      }
    }

    override def analysis(classpathEntry: File): Optional[CompileAnalysis] = Optional.empty()
  }

  // TODO: Remove duplication once on Scala 2.12.x.
  val positionMapper =
    new JFunction[Position, Position] {
      override def apply(p: Position): Position = p
    }


  val reporter =
    ReporterUtil.getDefault(
      ReporterConfig.create(
        "",
        Int.MaxValue,
        true,
        Array.empty[JFunction[String, JBoolean]],
        Array.empty[JFunction[java.nio.file.Path, JBoolean]],
        java.util.logging.Level.INFO,
        positionMapper
      )
    )

  val compilerJar = new File("/home/humblehound/Dev/fsbt/backend/lib/compiler-bridge_2.12-1.0.0.jar")

  def getBridge = {
    val qq = ZincUtil.constantBridgeProvider(scalaInstance, compilerJar)
    qq.fetchCompiledBridge(scalaInstance, zincLogger)
  }

  /**
    * Create a new classloader with the root loader as parent (to avoid zinc itself being included).
    */
  def scalaLoader(jars: Seq[File]) =
    new URLClassLoader(
      Path.toURLs(jars),
      sbt.internal.inc.classpath.ClasspathUtilities.rootLoader
    )

  def scalaVersion(scalaLoader: ClassLoader): Option[String] = {
    Util.propertyFromResource("compiler.properties", "version.number", scalaLoader)
  }

  def compiler: AnalyzingCompiler = ZincUtil.scalaCompiler(scalaInstance, getBridge)
  def scalaInstance = {

    val scalaHome = sys.env("SCALA_HOME")

    val libJar = new File(s"$scalaHome/lib/scala-library.jar")
    val compileJar = new File(s"$scalaHome/lib/scala-compiler.jar")
    val reflectJar = new File(s"$scalaHome/lib/scala-reflect.jar")

    val allJars = Array(libJar, compileJar, reflectJar)

    val loader = scalaLoader(allJars)

    new ScalaInstance(scalaVersion(loader).getOrElse("unknown"), loader, libJar, compileJar, allJars, Option.empty)
  }


  def javaTools = JavaTools(JavaCompiler.fork(), Javadoc.fork())
}
