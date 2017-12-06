package core.tasks

import com.martiansoftware.nailgun.NGContext
import core.config.FsbtProject

trait Task {
  def perform(config: FsbtProject)(implicit ctx: NGContext): Unit
}
