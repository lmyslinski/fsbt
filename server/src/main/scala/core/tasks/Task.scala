package core.tasks

import com.martiansoftware.nailgun.NGContext
import core.config.FsbtConfig

trait Task {
  def perform(config: FsbtConfig)(implicit ctx: NGContext): Unit
}
