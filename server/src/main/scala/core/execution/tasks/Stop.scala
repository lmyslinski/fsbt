package core.execution.tasks
import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config.FsbtModule
import core.config.compile.ExecutionConfig
import core.execution.Task

case class Stop() extends Task {
  def perform(config: FsbtModule)(implicit ctx: NGContext, logger: Logger): Unit = {
    ctx.getNGServer.shutdown(true)
    logger.debug("fsbt server stopped")
  }

  override def perform(module: FsbtModule,config: ExecutionConfig, moduleTaskCompleted: FsbtModule => Unit)(implicit ctx: NGContext, logger: Logger): Unit = {

  }
}