package core

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config._
import core.tasks._
import util.LazyNailLogging


object Fsbt extends LazyNailLogging {

  def main(args: Array[String]): Unit = {
    println("Not running as nailgun! Exiting")
  }

  def nailMain(context: NGContext): Unit = {

    implicit val logger: Logger = getLogger(context)
    implicit val ctx: NGContext = context

    val args = context.getArgs.toList
    if(args.length == 1 && args.head == "stop"){
      context.getNGServer.shutdown(true)
    }

    val tasks = args.flatMap {
      case "stop" => List(new Stop())
      case "compile" => List(new Compile())
      case "test" => List(new Compile(), new Test())
      case "run" => List(new Compile, new Run())
      case "package" => List(new Compile(), new Test(), new JarPackage())
      case "clean" => List(new Clean())
      case unknown =>
        context.out.println(s"Command not found: $unknown")
        List()
    }

    val config = ConfigBuilder.build(context)
    tasks.foreach(_.perform(config))
  }
}
