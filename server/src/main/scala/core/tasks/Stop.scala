package core.tasks
import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config.FsbtModule

class Stop extends Task {
  def perform(config: FsbtModule)(implicit ctx: NGContext, logger: Logger): Unit = {
    ctx.getNGServer.shutdown(true)
    logger.debug("fsbt server stopped")
  }

  override def perform(module: FsbtModule, moduleTaskCompleted: FsbtModule => Unit)(implicit ctx: NGContext, logger: Logger): Unit = {

  }
}

object Stop{

}