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
    val config = ConfigBuilder.build(context)
    val args = context.getArgs.toList
    implicit val ctx: NGContext = context

    if (args.isEmpty) {
      context.out.println("fsbt@0.0.1")
      config.dependencies.foreach(context.out.println)
    } else
      args.foreach {
        case "stop" =>
          context.getNGServer.shutdown(true)
        case "compile" =>
          new Compile().perform(config)
        case "test" =>
          new Compile().perform(config)
          Test.perform(config)
        case "run" =>
          new Compile().perform(config)
          Run.perform(config)
        case "package" =>
          new Compile().perform(config)
          JarPackage.perform(config)
        case "clean" => Clean.perform(config)
        case unknown => context.out.println("command not found: " + unknown)
      }
  }

//  def executeTask(f: (List[String], FsbtConfig) => Unit, args: List[String], config: FsbtConfig): Unit = {
//    try {
//      f(args, config)
//    } catch {
//      case ex: Exception => logger.debug("Oops")
//    }
//  }

}
