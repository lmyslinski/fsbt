package compiler

import java.io.File
import java.lang.{Boolean => JBoolean}
import java.net.{URL, URLClassLoader}
import java.util.Optional
import java.util.function.{Supplier, Function => JFunction}

import better.files
import com.typesafe.scalalogging.Logger
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

    override def debug(msg: Supplier[String]): Unit = {
//      logger.debug(msg.get())
    }

    override def error(msg: Supplier[String]): Unit = logger.error(msg.get())

    override def warn(msg: Supplier[String]): Unit = {
//      logger.warn(msg.get())
    }

    override def trace(exception: Supplier[Throwable]): Unit = {
//      logger.trace("trace message", exception.get())
    }

    override def info(msg: Supplier[String]): Unit = logger.info(msg.get())
  }


  def compile(classPath: List[files.File], sourceFiles: List[files.File], target: files.File) = {
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

    val previousResult = PreviousResult.create(Optional.empty(), Optional.empty())

    val inputs = Inputs.create(
      compilers,
      CompileOptions.create().
        withClasspath(classPath.map(_.toJava).toArray)
        .withClassesDirectory(target.toJava)
        .withSources(sourceFiles.map(_.toJava).toArray),
      setup,
      previousResult)
    val cr = cp.compile(inputs, zincLogger)
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

  val compilerJar = getJar("compiler-bridge")

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
    propertyFromResource("compiler.properties", "version.number", scalaLoader)
  }

  /**
    * Get a property from a properties file resource in the classloader.
    */
  def propertyFromResource(resource: String, property: String, classLoader: ClassLoader): Option[String] = {
    val props = propertiesFromResource(resource, classLoader)
    Option(props.getProperty(property))
  }

  /**
    * Get all properties from a properties file resource in the classloader.
    */
  def propertiesFromResource(resource: String, classLoader: ClassLoader): java.util.Properties = {
    val props = new java.util.Properties
    val stream = classLoader.getResourceAsStream(resource)
    try { props.load(stream) }
    catch { case e: Exception => }
    finally { if (stream ne null) stream.close }
    props
  }

  def compiler: AnalyzingCompiler = ZincUtil.scalaCompiler(scalaInstance, getBridge)

  def getClasspathUrls(cl: ClassLoader): Array[java.net.URL] = cl match {
    case null => Array()
    case u: java.net.URLClassLoader => u.getURLs ++ getClasspathUrls(cl.getParent)
    case _ => getClasspathUrls(cl.getParent)
  }

  def urls: Array[URL] = getClasspathUrls(getClass.getClassLoader)
  val scalaHome = sys.env("SCALA_HOME")

  def getJar(name: String): File = {
    val classPathOption = urls.filter(_.toString.contains(name))
    if(classPathOption.length == 1){
      new File(classPathOption(0).getFile)
    }else{
      if(scalaHome != ""){
        new File(s"$scalaHome/lib/$name.jar")
      }else{
        throw new RuntimeException(s"Cannot locate $name jar")
      }
    }
  }

  def scalaInstance = {
    val libJar = getJar("scala-library")
    val compileJar = getJar("scala-compiler")
    val reflectJar = getJar("scala-reflect")
    val allJars = Array(libJar, compileJar, reflectJar)
    val loader = scalaLoader(allJars)
    new ScalaInstance(scalaVersion(loader).getOrElse("unknown"), loader, libJar, compileJar, allJars, Option.empty)
  }


  def javaTools = JavaTools(JavaCompiler.fork(), Javadoc.fork())
}
