package core

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config._
import core.config.compile.CompileConfig
import core.execution.{ExecutionHelper, TaskExecutor}
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

//    try{
      val modules = ModuleBuilder.buildModules(context)
      val executionConfig = ExecutionHelper.build(modules)
      val executor = new TaskExecutor(modules, executionConfig, new Compile())

      executor.execute()


//      executionConfig.foreach(x => println(s"${x.self} waits for ${x.waitFor} and notifies ${x.notifyOnComplete}"))

//      tasks.foreach(_.perform(config))
//    }catch{
//      case ex: Throwable => logger.error(ex.getMessage)
//    }

  }
}
