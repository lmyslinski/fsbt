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
import core.config.FsbtConfig
import org.slf4j.LoggerFactory
import xsbti.compile.IncOptions
import sbt.internal.inc.javac.{JavaCompiler, JavaTools, Javadoc}
import sbt.internal.inc.{AnalyzingCompiler, ScalaInstance, ZincUtil}
import sbt.io.Path
import xsbti._
import xsbti.compile._

/**
  * Created by humblehound on 24.07.17.
  */
class ZincCompiler {


  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  private val zincLogger = new xsbti.Logger {

    override def debug(msg: Supplier[String]): Unit = logger.debug(msg.get())

    override def error(msg: Supplier[String]): Unit = logger.error(msg.get())

    override def warn(msg: Supplier[String]): Unit = logger.warn(msg.get())

    override def trace(exception: Supplier[Throwable]): Unit = logger.trace(exception.get().getMessage, exception.get())

    override def info(msg: Supplier[String]): Unit = logger.info(msg.get())
  }


  def compile(classPath: Array[File], sourceFiles: Array[File], config: FsbtConfig): CompileResult = {

    val cp = ZincCompilerUtil.defaultIncrementalCompiler()
    val compilers = Compilers.create(compiler, javaTools)

    val setup = Setup.create(
      getPerClasspathEntryLookup,
      false,
      new File(FsbtConfig.zincCache),
      CompilerCache.fresh,
      IncOptions.create(),
      reporter,
      Optional.empty(),
      Array.empty)

    val previousResult = FsbtCache.getCompileResult(config)

    val inputs = Inputs.create(
      compilers,
      CompileOptions.create().withClasspath(classPath).withClassesDirectory(config.target.toJava)
        .withSources(sourceFiles),
      setup,
      previousResult)
    val cr = cp.compile(inputs, zincLogger)
    if(cr.hasModified){
      FsbtCache.updateCache(config, cr)
    }

    cr
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
        java.util.logging.Level.SEVERE,
        positionMapper
      )
    )

  def getBridge = {
    val qq = ZincUtil.constantBridgeProvider(ScalaLocator.scalaInstance, ScalaLocator.getJar("compiler-bridge"))
    qq.fetchCompiledBridge(ScalaLocator.scalaInstance, zincLogger)
  }

  /**
    * Create a new classloader with the root loader as parent (to avoid zinc itself being included).
    */






  def compiler: AnalyzingCompiler = ZincUtil.scalaCompiler(ScalaLocator.scalaInstance, getBridge)





  def javaTools = JavaTools(JavaCompiler.fork(), Javadoc.fork())
}

