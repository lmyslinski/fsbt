package core.dependencies
import java.io._
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}
import java.util.stream.Collectors

import better.files.File

import scala.xml.{Source, XML}


case class MavenDependency(groupId: String, artifactId: String, version: String) {
  val mavenCentral = "http://central.maven.org/maven2"
  val wtf="org/scala-lang/modules/scala-parser-combinators_2.11/1.0.5/scala-parser-combinators_2.11-1.0.5.jar"
  val fsbtPath = System.getProperty("user.home") + "/.fsbt"
  val fsbtCache = s"$fsbtPath/cache"

  val scalaVersion = "_2.12"

  val groupIdParsed: String = stripQuotes(groupId.replace('.','/'))
  val artifactIdParsed: String = stripQuotes(artifactId)
  val versionParsed: String = stripQuotes(version)

  val baseUri = s"$mavenCentral/$groupIdParsed/$artifactIdParsed$scalaVersion/$versionParsed/$artifactIdParsed$scalaVersion-$versionParsed"
  val pomUrl = s"$baseUri.pom"
  val jarUri = s"$baseUri.jar"

  def downloadDependency(pomFile: File, jarFile: File): Unit = {
    if (!pomFile.exists && !jarFile.exists) {
      println("Downloading")
      val pom = downloadPom(pomFile)
      val jar = downloadJar(jarFile)
      downloadTransitiveDependencies(scala.xml.XML.loadString(pom))
    } else {
      println("Loading XML file")
     downloadTransitiveDependencies(XML.loadFile(pomFile.toJava))
    }
  }

  def downloadTransitiveDependencies(pom: scala.xml.Elem) ={
    val dependencies = pom \ "dependencies" \ "dependency"
    dependencies.foreach { dependency =>
      val groupId = (dependency \ "groupId").text.replace('.', '/')
      val artifactId = (dependency \ "artifactId").text
      val version = (dependency \ "version").text

      val depPomFile = File(s"$fsbtCache/$groupId/$artifactId/$version/pom.xml")
      val depJarFile = File(s"$fsbtCache/$groupId/$artifactId/$version/$artifactId.jar")

      if (!depPomFile.exists && !depJarFile.exists) {
        downloadDependency(depPomFile, depJarFile)
      }
    }
  }

  def downloadJar(jarFile: File): Unit ={

    val jarWebsite = new URL(pomUrl)
    val in2 = jarWebsite.openConnection().getInputStream
    Files.copy(in2, jarFile.path, StandardCopyOption.REPLACE_EXISTING)
  }

  def downloadPom(pomFile: File): String = {
    val website = new URL(pomUrl)
    val in = website.openConnection().getInputStream
    pomFile.parent.createDirectories()
    val is = scala.io.Source.fromInputStream(in)
    val pom = is.getLines().mkString("\n")

    Files.copy(in, pomFile.path, StandardCopyOption.REPLACE_EXISTING)

    val out = new PrintWriter(pomFile.path.toAbsolutePath.toString)
    out.println(pom)
    out.close()
    pom
  }

  def downloadDependencies(): Unit = {
    val pomFile = File(s"$fsbtCache/$groupIdParsed/$artifactIdParsed/$versionParsed/pom.xml")
    val jarFile = File(s"$fsbtCache/$groupIdParsed/$artifactIdParsed/$versionParsed/$artifactIdParsed.jar")
    downloadDependency(pomFile, jarFile)
  }


  def stripQuotes(string: String): String = string.replaceAll("^\"|\"$", "")
}
