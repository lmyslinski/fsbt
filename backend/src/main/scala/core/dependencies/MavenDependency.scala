package core.dependencies

import better.files.File
import com.typesafe.scalalogging.Logger
import core.config.{Dependency, FsbtConfig}
import org.slf4j.LoggerFactory

// exclusions are not supported for now
class MavenDependency(
  val groupId: String,
  val artifactId: String,
  val version: Option[String] = None,
  val withScalaVersion: Boolean = false,
  val scope: MavenDependencyScope.Value = MavenDependencyScope.Compile) {

  override def toString: String = descriptor

  def this(dependency: Dependency) = this(
    MavenDependency.stripQuotes(dependency.group).replace('.', '/'),
    MavenDependency.stripQuotes(dependency.artifact),
    Some(MavenDependency.stripQuotes(dependency.version)),
    withScalaVersion = dependency.withScalaVersion
  )

  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val pomFile = File(s"${FsbtConfig.fsbtCache}/$groupId/$artifactId/$version/pom.xml")
  val jarFile = File(s"${FsbtConfig.fsbtCache}/$groupId/$artifactId/$version/$artifactId.jar")
  val descriptor = s"$groupId/$artifactId/${version.get}"

  val baseUri: String = if (withScalaVersion) {

    val core = s"${MavenDependency.mavenCentral}/$groupId/$artifactId"

    s"${MavenDependency.mavenCentral}/$groupId/$artifactId${FsbtConfig.scalaVersion}/${version.get}/$artifactId${FsbtConfig.scalaVersion}-${version.get}"
  } else {
    s"${MavenDependency.mavenCentral}/$groupId/$artifactId/${version.get}/$artifactId-${version.get}"
  }

  val pomUrl = s"$baseUri.pom"
  val jarUrl = s"$baseUri.jar"

  override def equals(obj: scala.Any): Boolean = {
    val otherDep = obj.asInstanceOf[MavenDependency]
    otherDep.artifactId.equals(artifactId) &&
      otherDep.groupId.equals(groupId) &&
      otherDep.version.equals(version)
  }


}

object MavenDependency {
  val mavenCentral = "http://central.maven.org/maven2"

  def stripQuotes(string: String): String = string.replaceAll("^\"|\"$", "")
}

