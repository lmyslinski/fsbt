package compiler

import java.io.File
import java.net.{URL, URLClassLoader}

import sbt.internal.inc.ScalaInstance
import sbt.io.Path

object ScalaLocator {

  def scalaLoader(jars: Seq[File]) =
    new URLClassLoader(
      Path.toURLs(jars),
      sbt.internal.inc.classpath.ClasspathUtilities.rootLoader
    )

  val scalaHome = sys.env("SCALA_HOME")

  def scalaInstance = {
    val libJar = getJar("scala-library")
    val compileJar = getJar("scala-compiler")
    val reflectJar = getJar("scala-reflect")
    val allJars = Array(libJar, compileJar, reflectJar)
    val loader = scalaLoader(allJars)
    new ScalaInstance(scalaVersion(loader).getOrElse("unknown"), loader, libJar, compileJar, allJars, Option.empty)
  }

  def getJar(name: String): File = {
    val classPathOption = urls.filter(_.toString.contains(name))
    if (classPathOption.length == 1) {
      new File(classPathOption(0).getFile)
    } else {
      if (scalaHome != "") {
        new File(s"$scalaHome/lib/$name.jar")
      } else {
        throw new RuntimeException(s"Cannot locate $name jar")
      }
    }
  }

  def getClasspathUrls(cl: ClassLoader): Array[java.net.URL] = cl match {
    case null => Array()
    case u: java.net.URLClassLoader => u.getURLs ++ getClasspathUrls(cl.getParent)
    case _ => getClasspathUrls(cl.getParent)
  }

  def urls: Array[URL] = getClasspathUrls(getClass.getClassLoader)

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
    try {
      props.load(stream)
    }
    catch {
      case e: Exception =>
    }
    finally {
      if (stream ne null) stream.close
    }
    props
  }

}
