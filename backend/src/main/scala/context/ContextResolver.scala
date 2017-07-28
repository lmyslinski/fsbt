package context
import better.files.File

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

  def run(target: File): Unit = {
    val cls = transformClassFormat(className.get)
    val command = List("java",  "-cp", target.toString(), cls)
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