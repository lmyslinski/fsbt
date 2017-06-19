package context
import scala.sys.process._

/**
  * We could use Akka, but fuck it for now
  *
  */

class ContextResolver {

  var mainMethodFound = false
  var fileName: Option[String] = None
  var className: Option[String] = None
  var superClass: Option[String] = None

  def run(targetDir: String): Unit = {
    val cls = transformClassFormat(className.get)
    val command = List("scala",  "-cp", targetDir, cls)
    val output = command.lineStream
    output.foreach(println)
  }

  def transformClassFormat(packageString: String) = {
    packageString.replace('/', '.')
  }
}


object ContextResolver{
  val mainMethodName = "main"
  val mainMethodDesc = "([Ljava/lang/String;)V"
  val javaObject = "java/lang/Object"
}