package core.dependencies

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}

import better.files.File
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

import scala.collection.immutable.Seq
import scala.util.matching.Regex
import scala.xml.{Elem, NodeSeq, XML}

// first, download pom and jar for each dependency
// create a list of all first and second level deps
// choose a strategy for resolving version conflicts (default: higher wins)
// download all not-yet resolved jars
// persist an immutable classpath somehow

// also, add checksum verification...
// also, add parent pom support (FML)
object DependencyResolver {

  private val pomVariableRegex: Regex = """\$\{(.*)\}""".r

  def resolveAll(dependencies: List[MavenDependency])(implicit logger: Logger): List[MavenDependency] = {
    val a = dependencies.flatMap(dependency => resolveRecursive(dependency)) ::: dependencies
    val b = a.groupBy(f => (f.groupId, f.artifactId))
    val c = b.map{
      case ((group, artifact), deps) =>
        val winner = deps.reduce(max)
        winner.copyWith(version = winner.version)
    }.toList
    c
  }

  private def max(s1: MavenDependency, s2: MavenDependency): MavenDependency = if (s1.version > s2.version) s1 else s2


  private def resolveVariables(dependencies: List[MavenDependency], propertyMap: Map[String, String]): List[MavenDependency] = {

    dependencies.map { dependency =>
      val version = dependency.version match {
        case pomVariableRegex(variable) => propertyMap(variable)
        case _ => dependency.version
      }
      val groupId = dependency.groupId match {
        case pomVariableRegex(variable) => propertyMap(variable)
        case _ => dependency.groupId
      }
      val artifactId = dependency.artifactId match {
        case pomVariableRegex(variable) => propertyMap(variable)
        case _ => dependency.artifactId
      }
      dependency.copyWith(groupId, artifactId, version)
    }
  }

  private def updateDependenciesFromDependencyManagement(pomFile: PomFile): PomFile = {
    if(pomFile.parentPom.isDefined){
      val dependencyManagement = pomFile.parentPom.get.dependencyManagement
      val (toUpdate, complete) = pomFile.dependencies.partition(p => p.version.isEmpty || p.scope == MavenDependencyScope.Import)
      val updated = complete ::: toUpdate.map{x =>
        if(dependencyManagement.isDefinedAt(x.groupId, x.artifactId)){
          dependencyManagement((x.groupId, x.artifactId))
        }else{
          x
        }
      }
      pomFile.copy(dependencies = updated)
    }else pomFile
  }

  def getRecursiveProperties(pomFile: PomFile) : Map[String, String] = {
    if(pomFile.parentPom.isDefined){
      pomFile.properties ++ getRecursiveProperties(pomFile.parentPom.get)
    }else{
      pomFile.properties
    }
  }

  def getRecursiveDepencies(pomFile: PomFile) : List[MavenDependency] = {
    if(pomFile.parentPom.isDefined){
      pomFile.dependencies ::: getRecursiveDepencies(pomFile.parentPom.get)
    }else{
      pomFile.dependencies
    }
  }


  private def resolveRecursive(dependency: MavenDependency)(implicit logger: Logger): List[MavenDependency] = {
//    logger.debug(s"Working on $dependency")
    val pomFileOption = getPom(dependency)
    if(pomFileOption.isDefined){
      val pomFile = pomFileOption.get
      val updatedPom = updateDependenciesFromDependencyManagement(pomFile)
      val allVariables = getRecursiveProperties(updatedPom)
      val allDependencies = getRecursiveDepencies(updatedPom)
      val resolved = resolveVariables(allDependencies, allVariables)
//      logger.debug(s"$dependency resolved, sending downsteam ${resolved}")
      resolved ::: resolved.flatMap(resolveRecursive)
    }else{
      List()
    }

  }

  private def getParentPom(pom: Elem)(implicit logger: Logger): Option[PomFile] = {
    val parent = pom \ "parent"
    if (parent.nonEmpty) getPom(toMavenDependency(parent)) else None
  }

  private def getPom(dependency: MavenDependency)(implicit logger: Logger): Option[PomFile] = {
    try{
      val file = if (!dependency.pomFile.exists) {
        downloadPom(dependency).toJava
      }else dependency.pomFile.toJava
      val pomFile = XML.loadFile(file)
      val parent = getParentPom(pomFile)
      val dependencies = getDependenciesFromPom(pomFile)
      val dependencyManagement = getDependencyManagementFromPom(pomFile)
      val propertyMap = getPropertyMap(pomFile)
      Some(PomFile(dependencies, propertyMap, parent, dependencyManagement))
    }catch {
      case _: Throwable => {
//        logger.debug(s"Error, dependency $dependency not found")
        None
      }
    }
  }


  private def getDependenciesFromPom(pom: Elem): List[MavenDependency] =
    (pom \ "dependencies" \ "dependency").map(toMavenDependency)
      .filter(_.scope == MavenDependencyScope.Compile)
      .filter(_.optional == false).toList

  private def getDependencyManagementFromPom(pom: Elem): Map[(String, String), MavenDependency] =
    (pom \ "dependencyManagement" \ "dependencies" \ "dependency")
      .map(toMavenDependency)
      .map(p => ((p.groupId, p.artifactId), p)).toMap


  private def getPropertyMap(pom: Elem): Map[String, String] = {
    ((pom \ "properties" \ "_").map { property => property.label -> property.text }.toMap +
      ("project.artifactId" -> (pom \ "artifactId").text) +
      ("project.groupId" -> (pom \ "groupId").text) +
      ("project.version" -> (pom \ "version").text)).filter(_._2 != "")
  }

  private def downloadPom(dependency: MavenDependency)(implicit logger: Logger): File = {
    try {
//      logger.debug(s"Downloading ${dependency.pomUrl}")
      val website = new URL(dependency.pomUrl)
      val in = website.openConnection().getInputStream
      dependency.pomFile.parent.createDirectories()
      val is = scala.io.Source.fromInputStream(in)
      val pom = is.getLines().mkString("\n")

      Files.copy(in, dependency.pomFile.path, StandardCopyOption.REPLACE_EXISTING)

      val out = new PrintWriter(dependency.pomFile.path.toAbsolutePath.toString)
      out.println(pom)
      out.close()
    } catch {
      case ex: Exception =>
//        logger.debug(s"Could not download ${dependency.pomUrl}: ${ex.getMessage}")
    }
    dependency.pomFile
  }

  private def toMavenDependency(node: NodeSeq): MavenDependency = {
    val groupId = (node \ "groupId").text
    val artifactId = (node \ "artifactId").text
    val version = (node \ "version").text.toString

    val scope = (node \ "scope").text.toString match {
      case "" => MavenDependencyScope.Compile
      case scp => MavenDependencyScope.withName(scp)
    }

    val optional = (node \ "optional").text.toString match {
      case "" => false
      case _ => true
    }

    new MavenDependency(groupId, artifactId, version, scope = scope, optional = optional)
  }
}
