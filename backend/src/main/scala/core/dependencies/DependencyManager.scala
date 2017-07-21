package core.dependencies

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}

import better.files.File
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.immutable.Seq
import scala.xml.{Elem, Node, NodeSeq, XML}

/**
  * Created by humblehound on 21.07.17.
  */
class DependencyManager(val dependencies: List[MavenDependency]) {

  private val pomVariableRegex = """\$\{(.*)\}""".r
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def resolveAll(): Unit = {
    val pomFiles = dependencies.map{
      dep => {
        (dep, getPom(dep))
      }
    }
    for((dep, pom) <- pomFiles){
      logger.debug(s"Resolving ${dep.descriptor}")
      newEnrichPom(dep, pom)
    }
  }

  def getPom(dependency: MavenDependency): File = {
    if (!dependency.pomFile.exists) {
      downloadPom(dependency)
    }
    dependency.pomFile
  }

  // First, convert all entries to MavenDependencies
  // for each entry:
  // If version is not present, here comes the clusterfuck!
  // If parent exists:
    // check dependencyManagement in parent for the same entry - if version present
    // as variable, resolve it
    // if not, get the latest one from maven
  // else just get the latest from maven
  // if version is a variable, resolve it


  def resolveVariables(depMgmtParent: Seq[MavenDependency], ppropmap: Map[String, String]) = {
    depMgmtParent.map{ dependency =>
      dependency.version match {
        case pomVariableRegex(variable) =>
          new MavenDependency(dependency.groupId, dependency.artifactId, ppropmap(variable))
        case _ => dependency
      }
    }
  }

  def resolveVariablesWithParent(dependencies: Seq[MavenDependency],
                                 ownpropmap: Map[String, String],
                                 ppropmap: Map[String, String]) = {
    dependencies.map{ dependency =>
      dependency.version match {
        case pomVariableRegex(variable) =>
          val versionValue = ownpropmap.contains(variable) match{
            case true => ownpropmap(variable)
            case false => ppropmap(variable)
          }
          new MavenDependency(dependency.groupId, dependency.artifactId, variable)
        case _ => dependency
      }
    }
  }

  def getVersionFromParent(newDeps: Seq[MavenDependency], depMgmtParent: Map[(String, String), String]) = {
    newDeps.map{
      dependency => dependency.version match {
        case "" => new MavenDependency(
          dependency.groupId,
          dependency.artifactId,
          depMgmtParent((dependency.groupId, dependency.artifactId))
        )
        case _ => dependency
      }
    }
  }

  def newEnrichPom(dependency: MavenDependency, pomFile: File): PomInfo = {

    val pom = XML.loadFile(pomFile.toJava)
    val parentPom = getParentPom(pom)

    parentPom.isDefined match{
      case true =>
        val ppropmap = getPropertyMap(parentPom.get)
        val depMgmtParent = getDependencyManagement(parentPom.get)
        val resolved = resolveVariables(depMgmtParent, ppropmap).map{
          dependency => (dependency.groupId, dependency.artifactId) -> dependency.version
        }.toMap

        val pomDependencyEntries = pom \ "dependencies" \ "dependency"
        val pomPropertyMap = getPropertyMap(pom)
        val newDeps = pomDependencyEntries.map(toMavenDependency).filter(_.scope == MavenDependencyScope.Compile).filter(_.optional == false)
        val complete = resolveVariablesWithParent(newDeps, pomPropertyMap, ppropmap)
        val complete2 = getVersionFromParent(complete, resolved)
        complete2.foreach(println)
        PomInfo()
      case false =>
        val pomDependencyEntries = pom \ "dependencies" \ "dependency"
        val pomPropertyMap = getPropertyMap(pom)
        val newDeps = pomDependencyEntries.map(toMavenDependency).filter(_.scope == MavenDependencyScope.Compile).filter(_.optional == false)
        val complete = resolveVariables(newDeps, pomPropertyMap)
        complete.foreach(println)
        PomInfo()
    }
  }

  def getParentPom(pom: Elem) : Option[Elem] = {
    val parent = pom \ "parent"
    if(parent.nonEmpty){
      Some(XML.loadFile(getPom(toMavenDependency(parent)).toJava))
    }else{
      None
    }
  }

  def toMavenDependency(node: NodeSeq): MavenDependency = {
    val groupId = (node \ "groupId").text.replace('.', '/').toString
    val artifactId = (node \ "artifactId").text.toString
    val version = (node \ "version").text.toString

    val scope = (node \ "scope").text.toString match {
      case "" => MavenDependencyScope.Compile
      case sth => MavenDependencyScope.withName(sth)
    }

    val optional = (node \ "optional").text.toString match {
      case "" => false
      case sth => true
    }

    new MavenDependency(groupId, artifactId, version, scope = scope, optional = optional)
  }

  def getPropertyMap(pom: Elem): Map[String, String] = {
    (pom \ "properties" \ "_").map { property => property.label -> property.text }.toMap +
      ("project.version" -> (pom \ "version").text)
  }

  def getDependencyManagement(pom: Elem): Seq[MavenDependency] = {
    val deps = pom \ "dependencyManagement" \ "dependencies" \ "_"
    deps.map(toMavenDependency)
  }


  def downloadPom(dependency: MavenDependency): File = {

    try{
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
    }catch {
      case ex: Exception => logger.debug(s"Could not download ${dependency.pomUrl}: ${ex.getMessage}")
    }
  dependency.pomFile
  }


    // first, download pom and jar for each dependency
    // create a list of all first and second level deps
    // choose a strategy for resolving version conflicts (default: higher wins)
    // download all not-yet resolved jars
    // persist an immutable classpath somehow

    // also, add checksum verification...
    // also, add parent pom support (FML)

}
