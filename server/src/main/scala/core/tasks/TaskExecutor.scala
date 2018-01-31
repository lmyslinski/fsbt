package core.tasks

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config.FsbtModule
import core.config.FsbtModule.FsbtProjectRef
import core.config.compile.ExecutionConfig

class TaskExecutor(val modules: List[FsbtModule], val configs: List[ExecutionConfig], task: Task)(implicit ctx: NGContext, logger: Logger) {

  var notifs = modules.map(x => (x.projectName, List[FsbtProjectRef]())).toMap
  var started = List[FsbtProjectRef]()

  def execute() = {

    val notStarted = configs.filterNot(x => started.contains(x.self))

    val readyToStart = notStarted.filter(p => {
      notifs(p.self) == p.waitFor
    })

    readyToStart.par.foreach(x => {
      this.synchronized {
        started = started :+ x.self
      }
      val self = modules.filter(_.projectName == x.self).head
      task.perform(self, taskCompleted)
    })
  }

  def taskCompleted(module: FsbtModule) = {
    this.synchronized {
      notifs = notifs.updated(module.projectName, notifs(module.projectName) :+ module.projectName)
    }
  }
}
