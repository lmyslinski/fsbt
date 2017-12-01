package core.tasks

import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import core.config.FsbtConfig

object Clean extends Task with LazyLogging{
  override def perform(config: FsbtConfig)(implicit ctx: NGContext): Unit = {
    for (file <- config.target.list) {
      file.delete()
    }
  }
}
