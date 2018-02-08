package core.dependencies

import better.files.File
import com.typesafe.scalalogging.Logger
import core.config.FsbtModule
import org.slf4j.LoggerFactory

// exclusions are not supported for now
class MavenDependency(
  val groupId: String,
  val artifactId: String,
  val version: String,
  val optional: Boolean = false,
  val withScalaVersion: Boolean = false,
  val scope: MavenDependencyScope.Value = MavenDependencyScope.Compile) {

  override def toString: String = s"$descriptor"

  def copyWith(groupId: String = groupId, artifactId: String = artifactId, version: String = version): MavenDependency = {
    new MavenDependency(groupId, artifactId, version, optional, withScalaVersion, scope)
  }

  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val pomFile = File(s"${FsbtModule.fsbtCache}/$groupId/$artifactId/$version/pom.xml")
  val jarFile = File(s"${FsbtModule.fsbtCache}/$groupId/$artifactId/$version/$artifactId.jar")
  val descriptor = s"$groupId/$artifactId/$version"

  val baseUri: String = {

    val group = groupId.replace('.', '/')
    val artifact = artifactId
    val vrs = version

    if (withScalaVersion) {
      s"${MavenDependency.mavenCentral}/$group/$artifact${FsbtModule.scalaVersion}/$vrs/$artifact${FsbtModule.scalaVersion}-$vrs"
    } else {
      s"${MavenDependency.mavenCentral}/$group/$artifact/$version/$artifact-$vrs"
    }
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

