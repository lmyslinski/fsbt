package core.dependencies

import java.io.{FileNotFoundException, PrintWriter}
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}

import better.files.File
import com.typesafe.scalalogging.Logger
import core.config.Dependency
import org.slf4j.LoggerFactory

import scala.xml.XML

/**
  * Created by humblehound on 21.07.17.
  */
class DependencyManager(val dependencies: List[MavenDependency]) {


  val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def resolveAll() = {
    for (dep <- dependencies) {


    }

    val pomFiles = dependencies.map{
      dep =>{
        logger.debug(s"Resolving ${dep.descriptor}")
        getPom(dep.pomFile)
      }
    }
  }

  def getPom(pomFile: File): File = {
    if (!pomFile.exists) {
      downloadPom(pomFile)
    }
    pomFile
  }


  def resolve(transitive : Boolean): Seq[MavenDependency] = {

    logger.debug(s"Resolving $groupIdParsed/$artifactIdParsed/$versionParsed/$artifactIdParsed-$versionParsed")


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

    // first, download pom and jar for each dependency
    // create a list of all first and second level deps
    // choose a strategy for resolving version conflicts (default: higher wins)
    // download all not-yet resolved jars
    // persist an immutable classpath somehow

    // also, add checksum verification...
    // also, add parent pom support (FML)

}
