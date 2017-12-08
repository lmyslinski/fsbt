package compiler

import java.io.File
import java.lang.{Boolean => JBoolean}
import java.net.{URL, URLClassLoader}
import java.util.Optional
import java.util.function.{Supplier, Function => JFunction}

import better.files
import com.typesafe.scalalogging.Logger
import core.Fsbt.logger
import core.cache.FsbtCache
import core.config.FsbtProject
import org.slf4j.LoggerFactory
import xsbti.compile.IncOptions
import sbt.internal.inc.javac.{JavaCompiler, JavaTools, Javadoc}
import sbt.internal.inc.{AnalyzingCompiler, ScalaInstance, ZincUtil}
import sbt.io.Path
import xsbti._
import xsbti.compile._

class ZincCompiler {


  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  private val zincLogger = new xsbti.Logger {

    override def debug(msg: Supplier[String]): Unit = ()
    //logger.debug(msg.get())

    override def error(msg: Supplier[String]): Unit = logger.error(msg.get())

    override def warn(msg: Supplier[String]): Unit = ()
//      logger.warn(msg.get())

    override def trace(exception: Supplier[Throwable]): Unit = logger.trace(exception.get().getMessage, exception.get())

    override def info(msg: Supplier[String]): Unit = logger.info(msg.get())
  }


  // TODO: consider caching mini setup between launches, so that we don't get a fresh compilation with each run
  lazy val setup: Setup = Setup.create(
    getPerClasspathEntryLookup,
    false,
    new File(FsbtProject.zincCache),
    CompilerCache.fresh,
    IncOptions.create(),
    reporter,
    Optional.empty(),
    Array.empty)

  lazy val compilers: Compilers = Compilers.create(compiler, javaTools)
  lazy val cp: IncrementalCompiler = ZincCompilerUtil.defaultIncrementalCompiler()

  def compile(classPath: Array[File], sourceFiles: Array[File], config: FsbtProject): CompileResult = {

    val previousResult = FsbtCache.getCompileResult(config)

    val inputs = Inputs.create(
      compilers,
      CompileOptions.create().withClasspath(classPath).withClassesDirectory(config.target.toJava)
        .withSources(sourceFiles),
      setup,
      previousResult)
    val cr = cp.compile(inputs, zincLogger)
    if (cr.hasModified) {
      FsbtCache.updateCache(config, cr)
    }

    cr
  }

  private def getPerClasspathEntryLookup = new PerClasspathEntryLookup {

    override def definesClass(classpathEntry: File): DefinesClass = (className: String) => {
      logger.debug(s"checking $className on classpath")
      true
    }

    override def analysis(classpathEntry: File): Optional[CompileAnalysis] = Optional.empty()
  }

  private val positionMapper =
    new JFunction[Position, Position] {
      override def apply(p: Position): Position = p
    }


  private val reporter =
    ReporterUtil.getDefault(
      ReporterConfig.create(
        "",
        Int.MaxValue,
        true,
        Array.empty[JFunction[String, JBoolean]],
        Array.empty[JFunction[java.nio.file.Path, JBoolean]],
        java.util.logging.Level.SEVERE,
        positionMapper
      )
    )

  private def getBridge = {
    val qq = ZincUtil.constantBridgeProvider(ScalaLocator.scalaInstance, ScalaLocator.getJar("compiler-bridge"))
    qq.fetchCompiledBridge(ScalaLocator.scalaInstance, zincLogger)
  }


  def compiler: AnalyzingCompiler = ZincUtil.scalaCompiler(ScalaLocator.scalaInstance, getBridge)

  def javaTools = JavaTools(JavaCompiler.fork(), Javadoc.fork())
}

