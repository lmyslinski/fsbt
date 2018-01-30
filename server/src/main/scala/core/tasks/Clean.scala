package core.tasks

import better.files.File
import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import core.config.FsbtProject

import scala.annotation.tailrec

class Clean extends Task with LazyLogging{
  override def perform(config: FsbtProject)(implicit ctx: NGContext, logger: Logger): Unit = {

    def flatten(config: FsbtProject): List[File] = {
      config.target :: config.modules.flatMap(flatten)
    }

    @tailrec
    def clean(modules: List[File]): Unit = {
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


    if(config.target.exists){
      clean(flatten(config))
    }
  }
}