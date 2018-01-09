package core.tasks
import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config.FsbtProject

class Stop extends Task {
  override def perform(config: FsbtProject)(implicit ctx: NGContext, logger: Logger): Unit = {
    ctx.getNGServer.shutdown(true)
    logger.debug("fsbt server stopped")
  }
}

object Stop{

}