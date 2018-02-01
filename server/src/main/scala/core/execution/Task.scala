package core.execution

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config.FsbtModule
import core.config.compile.ExecutionConfig

trait Task {

  def perform(self: FsbtModule, executionConfig: ExecutionConfig, taskCompleted: FsbtModule => Unit)(implicit ctx: NGContext, logger: Logger): Unit
}
