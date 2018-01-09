package core.tasks
import com.martiansoftware.nailgun.NGContext
import com.typesafe.scalalogging.LazyLogging
import core.config.FsbtProject

class Stop extends Task with LazyLogging {
  override def perform(config: FsbtProject)(implicit ctx: NGContext): Unit = {
    ctx.getNGServer.shutdown(true)
    logger.debug("fsbt server stopped")
  }
}
