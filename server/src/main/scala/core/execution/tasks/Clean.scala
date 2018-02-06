package core.execution.tasks

import better.files.File
import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import core.config.FsbtModule
import core.config.compile.ExecutionConfig
import core.execution.Task

import scala.annotation.tailrec

case class Clean() extends Task with LazyLogging{

  override def perform(module: FsbtModule, config: ExecutionConfig, moduleTaskCompleted: FsbtModule => Unit)(implicit ctx: NGContext, logger: Logger): Unit = {
    clean(List(module.target))
  }


  @tailrec
  private def clean(modules: List[File]): Unit = {
    modules match{
      case head::tail =>
        for (file <- head.list) {
          file.delete()
          logger.debug(s"Deleted ${file.path}")
        }
        clean(tail)
      case Nil => ()
    }
  }
}
