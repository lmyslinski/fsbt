package core

import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import core.config._
import core.tasks._


object Fsbt extends LazyLogging {

  def main(args: Array[String]): Unit = {
    println("Not running as nailgun!")
  }

  def nailMain(context: NGContext): Unit = {
    val args = context.getArgs.toList
    if(args.length == 1 && args.head == "stop"){
      context.getNGServer.shutdown(true)
    }

    val config = ConfigBuilder.build(context)
    implicit val ctx: NGContext = context

    val deps = FsbtUtil.getNestedDependencies(config)

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

    tasks.foreach(_.perform(config))
  }
}
