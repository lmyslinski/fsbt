package core.dependencies
import java.io._
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}
import java.util.stream.Collectors

import better.files.File
import com.typesafe.scalalogging.Logger
import core.config.Dependency
import org.slf4j.LoggerFactory

import scala.xml.{Source, XML}

import scala.language.implicitConversions

// TODO : Add pom parent support
// TODO : Add scoping(compile, test, runtime)
case class MavenDependency(dependency: Dependency) {

  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val mavenCentral = "http://central.maven.org/maven2"
  val fsbtPath = System.getProperty("user.home") + "/.fsbt"
  val fsbtCache = s"$fsbtPath/cache"
  val scalaVersion = "_2.12"

  val groupIdParsed: String = stripQuotes(dependency.group.replace('.','/'))
  val artifactIdParsed: String = stripQuotes(dependency.artifact)
  val versionParsed: String = stripQuotes(dependency.version)

  val pomFile = File(s"$fsbtCache/$groupIdParsed/$artifactIdParsed/$versionParsed/pom.xml")
  val jarFile = File(s"$fsbtCache/$groupIdParsed/$artifactIdParsed/$versionParsed/$artifactIdParsed.jar")

  val descriptor = s"$groupIdParsed/$artifactIdParsed/$versionParsed"

  val baseUri: String = if (dependency.withScalaVersion) {
    s"$mavenCentral/$groupIdParsed/$artifactIdParsed$scalaVersion/$versionParsed/$artifactIdParsed$scalaVersion-$versionParsed"
  } else {
    s"$mavenCentral/$groupIdParsed/$artifactIdParsed/$versionParsed/$artifactIdParsed-$versionParsed"
  }

  val pomUrl = s"$baseUri.pom"
  val jarUrl = s"$baseUri.jar"

  override def equals(obj: scala.Any): Boolean = {
    val otherDep = obj.asInstanceOf[MavenDependency]
    otherDep.artifactIdParsed.equals(artifactIdParsed) &&
      otherDep.groupIdParsed.equals(groupIdParsed) &&
      otherDep.versionParsed.equals(versionParsed)
  }
}




