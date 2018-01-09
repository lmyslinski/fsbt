package compiler

import java.io.File
import java.lang.{Boolean => JBoolean}
import java.util.Optional
import java.util.function.{Supplier, Function => JFunction}

import ch.qos.logback.classic.Logger
import core.cache.FsbtCache
import core.config.FsbtProject
import sbt.internal.inc.javac.{JavaCompiler, JavaTools, Javadoc}
import sbt.internal.inc.{AnalyzingCompiler, ZincUtil}
import xsbti._
import xsbti.compile.{IncOptions, _}

class ZincCompiler {

  private def zincLogger(implicit logger: Logger) = new xsbti.Logger {

    override def debug(msg: Supplier[String]): Unit = ()
//      logger.debug(msg.get())

    override def error(msg: Supplier[String]): Unit = ()
//    logger.error(msg.get())

    override def warn(msg: Supplier[String]): Unit = ()
//      logger.warn(msg.get())

    override def trace(exception: Supplier[Throwable]): Unit = logger.trace(exception.get().getMessage, exception.get())

    override def info(msg: Supplier[String]): Unit = logger.info(msg.get())
  }


  // TODO: consider caching mini setup between launches, so that we don't get a fresh compilation with each launch
  def setup(implicit logger: Logger): Setup = Setup.create(
    getPerClasspathEntryLookup,
    false,
    new File(FsbtProject.zincCache),
    CompilerCache.fresh,
    IncOptions.create(),
    reporter,
    Optional.empty(),
    Array.empty)

  def compilers(implicit logger: Logger): Compilers = Compilers.create(compiler, javaTools)
  lazy val cp: IncrementalCompiler = ZincCompilerUtil.defaultIncrementalCompiler()

  def compile(classPath: Array[File], sourceFiles: Array[File], config: FsbtProject)(implicit logger: Logger): Option[CompileResult] = {

    val previousResult = FsbtCache.getCompileResult(config)

    val inputs = Inputs.create(
      compilers,
      CompileOptions.create().withClasspath(classPath).withClassesDirectory(config.target.toJava)
        .withSources(sourceFiles),
      setup,
      previousResult)
    try{
      val cr = cp.compile(inputs, zincLogger)
       if (cr.hasModified) {
         FsbtCache.updateCache(config, cr)
       }
      Some(cr)
    }catch{
      case ex: Exception => logger.debug("FKC", ex)
      None
    }


  }

  private def getPerClasspathEntryLookup(implicit logger: Logger) = new PerClasspathEntryLookup {

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

  private def getBridge(implicit logger: Logger) = {
    val qq = ZincUtil.constantBridgeProvider(ScalaLocator.scalaInstance, ScalaLocator.getJar("compiler-bridge"))
    qq.fetchCompiledBridge(ScalaLocator.scalaInstance, zincLogger)
  }


  def compiler(implicit logger: Logger): AnalyzingCompiler = ZincUtil.scalaCompiler(ScalaLocator.scalaInstance, getBridge)

  def javaTools = JavaTools(JavaCompiler.fork(), Javadoc.fork())
}

