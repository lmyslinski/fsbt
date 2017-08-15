package compiler

import java.io.File
import java.net.URLClassLoader
import java.util.Optional
import java.util.function.{Function, Supplier}

import better.files
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import sbt.internal.inc.javac.{JavaCompiler, JavaTools, Javadoc}
import sbt.internal.inc.{AnalyzingCompiler, IncrementalCompilerImpl, ScalaInstance, ZincUtil}
import xsbti._
import xsbti.compile._

/**
  * Created by humblehound on 24.07.17.
  */
class ZincCompiler {

  def compile(classPath: List[files.File], sourceFiles: List[files.File], target: files.File) = {
    val cp = ZincCompilerUtil.defaultIncrementalCompiler()

    val compilers = Compilers.create(compiler, javaTools)

    val setup = Setup.create(getPerClasspathEntryLookup, false, new File(""), getGlobalsCache, IncOptions.create(), getReporter, Optional.empty(), Array.empty)

    val previousResult = PreviousResult.create(Optional.empty(), Optional.empty())

    val inputs = Inputs.create(compilers,
      CompileOptions.create().withClasspath(classPath.map(_.toJava).toArray).withClassesDirectory(target.toJava).withSources(sourceFiles.map(_.toJava).toArray),
      setup,
      previousResult)
    cp.compile(inputs, getLogger)

//    new IncrementalCompilerImpl().compile(inputs, getLogger)
  }

  def getSourcePositionMapper = new Function[Position, Position]() {
    override def apply(a: Position): Position = a
  }

  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def getLogger: xsbti.Logger = {
    new xsbti.Logger {

      override def debug(msg: Supplier[String]): Unit = logger.debug(msg.get())

      override def error(msg: Supplier[String]): Unit = logger.error(msg.get())

      override def warn(msg: Supplier[String]): Unit = logger.warn(msg.get())

      override def trace(exception: Supplier[Throwable]): Unit = logger.trace("", exception.get())

      override def info(msg: Supplier[String]): Unit = logger.info(msg.get())
    }
  }

  def getGlobalsCache = new GlobalsCache {

    override def apply(args: Array[String], output: Output, forceNew: Boolean, provider: CachedCompilerProvider, logger: xsbti.Logger, reporter: Reporter): CachedCompiler = new CachedCompiler {
      override def run(sources: Array[File], changes: DependencyChanges, callback: AnalysisCallback, logger: xsbti.Logger, delegate: Reporter, progress: CompileProgress): Unit = {

      }

      override def commandArguments(sources: Array[File]): Array[String] = Array.empty[String]
    }

    override def clear(): Unit = ()
  }

  def getPerClasspathEntryLookup = new PerClasspathEntryLookup {

    override def definesClass(classpathEntry: File): DefinesClass = new DefinesClass {
      override def apply(className: String): Boolean = false
    }

    override def analysis(classpathEntry: File): Optional[CompileAnalysis] = Optional.empty()
  }

  def getReporter = new Reporter {

    override def hasErrors: Boolean = false

    override def log(problem: Problem): Unit = logger.debug(problem.message())

    override def printSummary(): Unit = ()

    override def hasWarnings: Boolean = false

    override def reset(): Unit = ()

    override def comment(pos: Position, msg: String): Unit = ()

    override def problems(): Array[Problem] = Array.empty
  }

  val compilerJar = new File("/home/humblehound/.ivy2/cache/org.scala-sbt/compiler-bridge_2.12/jars/compiler-bridge_2.12-1.0.0.jar")

  def getBridge = {
    val qq = ZincUtil.constantBridgeProvider(scalaInstance, compilerJar)
    qq.fetchCompiledBridge(scalaInstance, getLogger)
  }

  def compiler: AnalyzingCompiler = ZincUtil.scalaCompiler(scalaInstance, getBridge)
  def scalaInstance = {

    val libJar = new File("/home/humblehound/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.12.2.jar")
    val compileJar = new File("/home/humblehound/.ivy2/cache/org.scala-lang/scala-compiler/jars/scala-compiler-2.12.2.jar")
    val reflectJar = new File("/home/humblehound/.ivy2/cache/org.scala-lang/scala-reflect/jars/scala-reflect-2.12.2.jar")

    val allJars = Array(libJar, compileJar, reflectJar)

    def loader = new URLClassLoader(allJars.map(_.toURI.toURL))

    new ScalaInstance("2.12.2", loader, libJar, compileJar, allJars, Option.empty)
  }


  def javaTools = JavaTools(JavaCompiler.fork(), Javadoc.fork())
}
