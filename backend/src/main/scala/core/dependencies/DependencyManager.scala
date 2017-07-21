package core.dependencies

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}

import better.files.File
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.xml.{Node, NodeSeq, XML}

/**
  * Created by humblehound on 21.07.17.
  */
class DependencyManager(val dependencies: List[MavenDependency]) {

  private val pomVariableRegex = """\$\{.*\}""".r
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


  def newEnrichPom(dependency: MavenDependency, pomFile: File): PomInfo = {

    val pom = XML.loadFile(pomFile.toJava)

    val pomDependencyEntries = pom \ "dependencies" \ "dependency"

//    pomDependencyEntries.foreach(p => logger.debug(p.toString()))

    val newDeps = pomDependencyEntries.map(mavenDependencyFromXml).filter(_.scope == MavenDependencyScope.Compile)
    newDeps.foreach(println)



//    logger.debug()

//    if(variablesPresent(pomDependencyEntries)){
//
//    }

    PomInfo()
  }

  def mavenDependencyFromXml(entry: Node): MavenDependency = {
      val groupId = (entry \ "groupId").text.toString
      val artifactId = (entry \ "artifactId").text.toString
      val version = (entry \ "version").text.toString match {
        case "" => None
        case x: String => Some(x)
      }
      val scope = (entry \ "scope").text.toString match {
        case "" => MavenDependencyScope.Compile
        case sth => MavenDependencyScope.withName(sth)
      }
      new MavenDependency(groupId, artifactId, version, scope = scope)
  }


  def variablesPresent(pom: NodeSeq): Boolean = pom.exists(p => p.text match {
    case pomVariableRegex(qq) => true
    case _ => false
  })

//  def enrichDependencyVariables(pom: Elem, dependencyEntries: NodeSeq) = {
//    val propertyMap = (pom \ "properties" \ "_").map{ property => property.label -> property.text}.toMap
//
//    val result = sigh.map{ p =>
//      logger.debug(p.text)
//      p.text match {
//        case regex(qq) => propertyMap(qq)
//        case _ => p.text
//      }
//    }
//  }



//  def enrichPom(dependency: MavenDependency, pomFile: File): PomInfo = {
//    val pom = XML.loadFile(pomFile.toJava)
//    val pomParent = getPomParent(pom)
//
//    if(pomParent.isDefined){
//      val pomParentXml = XML.loadFile(getPom(pomParent.get).toJava)
//      val propertyMap = (pomParentXml \ "properties" \ "_").map{ property => property.label -> property.text}.toMap
//
//
//
//
////      val seq = regex.findAllIn(pom.toString()).
////      seq.foreach(println)
////      logger.debug(pom.toString())
//
//      val sigh = pom \ "dependencies" \ "dependency" \ "_"
//
////      logger.debug(sigh.toString())
//
//      /*val result = sigh.map{ p =>
//         logger.debug(p.text)
//         p.text match {
//          case regex(qq) => propertyMap(qq)
//          case _ => p.text
//        }
//      }*/
//    }
//
//
//    val transitiveDependencies = pom \ "dependencies" \ "dependency"
//    PomInfo()
//  }

//  def getPomParent(pom: Elem) : Option[MavenDependency] = {
//    val parent = pom \ "parent"
//    if(parent.nonEmpty){
//      val groupId = (parent \ "groupId").text.replace('.', '/').toString
//      val artifactId = (parent \ "artifactId").text.toString
//      val version = (parent \ "version").text.toString
//      Some(MavenDependency(Dependency(groupId, artifactId, version, false)))
//    }else{
//      None
//    }
//  }


//  def resolve(transitive : Boolean): Seq[MavenDependency] = {
//
//    if (!jarFile.exists) {
//      downloadJar(jarFile)
//    }
//
//    if(transitive && pomFile.exists){
//      val pom = XML.loadFile(pomFile.toJava)
//      val transitiveDependencies = pom \ "dependencies" \ "dependency"
//      //      logger.debug(s"Transitive deps:")
//      //      logger.debug(s"$transitiveDependencies")
//      val filteredDependencies = (pom \ "dependencies" \ "dependency")
//        .filter{
//          dependency => (dependency \ "scope").text match {
//            case "" => true
//            case "compile" => true
//            case _ => false
//          }
//        }.filter {
//        dependency => (dependency \ "optional").text match {
//          case "true" => false
//          case _ => true
//        }
//      }.
//        map { dependency =>
//          val groupId = (dependency \ "groupId").text.replace('.', '/')
//          val artifactId = (dependency \ "artifactId").text
//          val version = (dependency \ "version").text
//          MavenDependency(Dependency(groupId, artifactId, version, withScalaVersion = false))
//        }.filter{p => p != this}
//
//      //      logger.debug(s"Filtered deps:")
//      //      logger.debug(s"$filteredDependencies")
//
//      if(filteredDependencies.nonEmpty){
//        Seq(this) ++ filteredDependencies.flatMap(_.resolve(false))
//      }else{
//        Seq(this)
//      }
//    }else{
//      Seq(this)
//    }
//  }
//
//  private def resolveTransitive() : MavenDependency = {
//    try{
//      if (!pomFile.exists) {
//        downloadPom(pomFile)
//      }
//    } catch {
//      case ex: FileNotFoundException => logger.debug(s"$pomFile does not exist")
//    }
//
//    try{
//      if (!jarFile.exists) {
//        downloadJar(jarFile)
//      }
//    } catch {
//      case ex: FileNotFoundException => logger.debug(s"$jarFile does not exist")
//    }
//    this
//  }
//
//  def downloadJar(jarFile: File): File ={
//    try{
//      logger.debug(s"Downloading $jarUrl")
//      val jarWebsite = new URL(jarUrl)
//      val in2 = jarWebsite.openConnection().getInputStream
//      Files.copy(in2, jarFile.path, StandardCopyOption.REPLACE_EXISTING)
//    }catch {
//      case ex: Exception => logger.debug(s"Could not download $jarUrl: ${ex.getMessage}")
//    }
//    jarFile
//  }
//
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
