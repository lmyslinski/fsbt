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

  val baseUri: String = if (dependency.withScalaVersion) {
    s"$mavenCentral/$groupIdParsed/$artifactIdParsed$scalaVersion/$versionParsed/$artifactIdParsed$scalaVersion-$versionParsed"
  } else {
    s"$mavenCentral/$groupIdParsed/$artifactIdParsed/$versionParsed/$artifactIdParsed-$versionParsed"
  }

  val pomUrl = s"$baseUri.pom"
  val jarUrl = s"$baseUri.jar"


  def resolve(transitive : Boolean): Seq[MavenDependency] = {

    logger.debug(s"Resolving $groupIdParsed/$artifactIdParsed/$versionParsed/$artifactIdParsed-$versionParsed")

    if (!pomFile.exists) {
      downloadPom(pomFile)
    }
    if (!jarFile.exists) {
      downloadJar(jarFile)
    }

    if(transitive && pomFile.exists){
      val pom = XML.loadFile(pomFile.toJava)
      val transitiveDependencies = pom \ "dependencies" \ "dependency"
//      logger.debug(s"Transitive deps:")
//      logger.debug(s"$transitiveDependencies")
      val filteredDependencies = (pom \ "dependencies" \ "dependency")
        .filter{
          dependency => (dependency \ "scope").text match {
            case "" => true
            case "compile" => true
            case _ => false
          }
        }.filter {
        dependency => (dependency \ "optional").text match {
          case "true" => false
          case _ => true
        }
      }.
      map { dependency =>
        val groupId = (dependency \ "groupId").text.replace('.', '/')
        val artifactId = (dependency \ "artifactId").text
        val version = (dependency \ "version").text
        MavenDependency(Dependency(groupId, artifactId, version, withScalaVersion = false))
      }.filter{p => p != this}

//      logger.debug(s"Filtered deps:")
//      logger.debug(s"$filteredDependencies")

      if(filteredDependencies.nonEmpty){
        Seq(this) ++ filteredDependencies.flatMap(_.resolve(false))
      }else{
        Seq(this)
      }
    }else{
      Seq(this)
    }
  }

  private def resolveTransitive() : MavenDependency = {
    try{
      if (!pomFile.exists) {
        downloadPom(pomFile)
      }
    } catch {
      case ex: FileNotFoundException => logger.debug(s"$pomFile does not exist")
    }

    try{
      if (!jarFile.exists) {
        downloadJar(jarFile)
      }
    } catch {
      case ex: FileNotFoundException => logger.debug(s"$jarFile does not exist")
    }
    this
  }

  def downloadJar(jarFile: File): File ={
    try{
      logger.debug(s"Downloading $jarUrl")
      val jarWebsite = new URL(jarUrl)
      val in2 = jarWebsite.openConnection().getInputStream
      Files.copy(in2, jarFile.path, StandardCopyOption.REPLACE_EXISTING)
    }catch {
      case ex: Exception => logger.debug(s"Could not download $jarUrl: ${ex.getMessage}")
    }
    jarFile
  }

  def downloadPom(pomFile: File): File = {

    try{
      logger.debug(s"Downloading $pomUrl")
      val website = new URL(pomUrl)
      val in = website.openConnection().getInputStream
      pomFile.parent.createDirectories()
      val is = scala.io.Source.fromInputStream(in)
      val pom = is.getLines().mkString("\n")

      Files.copy(in, pomFile.path, StandardCopyOption.REPLACE_EXISTING)

      val out = new PrintWriter(pomFile.path.toAbsolutePath.toString)
      out.println(pom)
      out.close()
    }catch {
      case ex: Exception => logger.debug(s"Could not download $pomUrl: ${ex.getMessage}")
    }
    pomFile
  }

  def stripQuotes(string: String): String = string.replaceAll("^\"|\"$", "")

  override def equals(obj: scala.Any): Boolean = {
    val otherDep = obj.asInstanceOf[MavenDependency]
    otherDep.artifactIdParsed.equals(artifactIdParsed) &&
      otherDep.groupIdParsed.equals(groupIdParsed) &&
      otherDep.versionParsed.equals(versionParsed)
  }
}




