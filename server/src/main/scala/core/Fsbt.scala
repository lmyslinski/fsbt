package core

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config._
import core.execution.{ExecutionHelper, Task, TaskExecutor}
import core.execution.tasks._
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

    val tasks: List[Task] = args.flatMap {
      case "stop" => List(Stop())
      case "compile" => List(Compile())
      case "test" => List(Compile(), Test())
      case "run" => List(Compile(), Run())
      case "package" => List(Compile(), Test(), JarPackage())
      case "clean" => List(Clean())
      case unknown =>
        context.out.println(s"Command not found: $unknown")
        List()
    }

    try{
      val modules = ModuleBuilder.buildModules(context)
      val executionConfig = ExecutionHelper.build(modules)
      tasks.foreach(new TaskExecutor(modules, executionConfig, _).execute())
    }catch{
      case ex: Throwable => logger.error(ex.getMessage)
    }
  }
}
