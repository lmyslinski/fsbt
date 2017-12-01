package core.dependencies

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}

import better.files.File
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.immutable.Seq
import scala.xml.{Elem, NodeSeq, XML}

class DependencyResolver(dependencies: List[MavenDependency]) {

  private val pomVariableRegex = """\$\{(.*)\}""".r
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def resolveAll(): List[MavenDependency] = {
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


  private def resolveVariables(depMgmtParent: Seq[MavenDependency], ppropmap: Map[String, String]): Seq[MavenDependency] = {
    depMgmtParent.map { dependency =>
      val version = dependency.version match {
        case pomVariableRegex(variable) => ppropmap(variable)
        case _ => dependency.version
      }
      val groupId = dependency.groupId match {
        case pomVariableRegex(variable) => ppropmap(variable)
        case _ => dependency.groupId
      }
      val artifactId = dependency.artifactId match {
        case pomVariableRegex(variable) => ppropmap(variable)
        case _ => dependency.artifactId
      }
      dependency.copyWith(groupId, artifactId, version)
    }
  }

  private def resolveVariablesWithParent(dependencies: Seq[MavenDependency], ownpropmap: Map[String, String],
                                 parentPropertyMap: Map[String, String]): Seq[MavenDependency] = {
    dependencies.map { dependency =>
      val version = dependency.version match {
        case pomVariableRegex(variable) => if (ownpropmap.contains(variable)) ownpropmap(variable) else parentPropertyMap(variable)
        case _ => dependency.version
      }
      val groupId = dependency.groupId match {
        case pomVariableRegex(variable) => if (ownpropmap.contains(variable)) ownpropmap(variable) else parentPropertyMap(variable)
        case _ => dependency.groupId
      }
      val artifactId = dependency.artifactId match {
        case pomVariableRegex(variable) => if (ownpropmap.contains(variable)) ownpropmap(variable) else parentPropertyMap(variable)
        case _ => dependency.artifactId
      }
      dependency.copyWith(groupId, artifactId, version)
    }
  }

  private def getVersionFromParent(newDeps: Seq[MavenDependency], depMgmtParent: Map[(String, String), String]): Seq[MavenDependency] = {
    newDeps.map {
      dependency =>
        dependency.version match {
          case "" => dependency.copyWith(dependency.groupId,
            dependency.artifactId,
            depMgmtParent(dependency.groupId, dependency.artifactId))
          case _ => dependency
        }
    }
  }

  private def resolveRecursive(dependency: MavenDependency): List[MavenDependency] = {
    val pom = XML.loadFile(getPom(dependency).toJava)
    val parentPom = getParentPom(pom)

    if (parentPom.isDefined) {
      val parentPropertyMap = getPropertyMap(parentPom.get)
      val parentDependencies = getParentDependencies(parentPom.get, parentPropertyMap)
      val dependencies = getDependenciesFromPom(pom)
      val propertyMap = getPropertyMap(pom)
      val completeDependencies = getVersionFromParent(resolveVariablesWithParent(dependencies, propertyMap, parentPropertyMap), parentDependencies).toList
      completeDependencies ::: completeDependencies.flatMap(resolveRecursive)
    } else {
      val dependencies = getDependenciesFromPom(pom)
      val propertyMap = getPropertyMap(pom)
      val completeDependencies = resolveVariables(dependencies, propertyMap).toList
      completeDependencies ::: completeDependencies.flatMap(resolveRecursive)
    }
  }

  private def getParentDependencies(parentPom: Elem, parentPropertyMap: Map[String, String]): Map[(String, String), String] = {
    val dependencyManagement = getDependencyManagement(parentPom)
    resolveVariables(dependencyManagement, parentPropertyMap).map {
      dependency => (dependency.groupId, dependency.artifactId) -> dependency.version
    }.toMap
  }


  private def getDependenciesFromPom(pom: Elem): Seq[MavenDependency] =
    (pom \ "dependencies" \ "dependency").map(toMavenDependency)
      .filter(_.scope == MavenDependencyScope.Compile)
      .filter(_.optional == false)


  private def getPropertyMap(pom: Elem): Map[String, String] = {



    ((pom \ "properties" \ "_").map { property => property.label -> property.text }.toMap +
      ("project.artifactId" -> (pom \ "artifactId").text) +
      ("project.groupId" -> (pom \ "groupId").text) +
      ("project.version" -> (pom \ "version").text)).filter(_._2 != "")
  }

  private def getDependencyManagement(pom: Elem): Seq[MavenDependency] = {
    val deps = pom \ "dependencyManagement" \ "dependencies" \ "_"
    deps.map(toMavenDependency)
  }

  private def getParentPom(pom: Elem): Option[Elem] = {
    val parent = pom \ "parent"
    if (parent.nonEmpty) Some(XML.loadFile(getPom(toMavenDependency(parent)).toJava)) else None
  }

  private def getPom(dependency: MavenDependency): File = {
    if (!dependency.pomFile.exists) {
      downloadPom(dependency)
    }
    dependency.pomFile
  }

  private def downloadPom(dependency: MavenDependency): File = {
    try {
      logger.debug(s"Downloading ${dependency.pomUrl}")
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
      case ex: Exception => logger.debug(s"Could not download ${dependency.pomUrl}: ${ex.getMessage}")
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


  // first, download pom and jar for each dependency
  // create a list of all first and second level deps
  // choose a strategy for resolving version conflicts (default: higher wins)
  // download all not-yet resolved jars
  // persist an immutable classpath somehow

  // also, add checksum verification...
  // also, add parent pom support (FML)

}
