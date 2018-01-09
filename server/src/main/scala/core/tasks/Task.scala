package core.tasks

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config.FsbtProject

trait Task {
  def perform(config: FsbtProject)(implicit ctx: NGContext, logger: Logger): Unit
}
