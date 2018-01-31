package core.tasks

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config.FsbtModule
import core.config.FsbtModule.FsbtProjectRef
import core.config.compile.ExecutionConfig

class TaskExecutor(val modules: List[FsbtModule], val configs: List[ExecutionConfig], task: Task)(implicit ctx: NGContext, logger: Logger) {

  var notifs = modules.map(x => (x.projectName, List[FsbtProjectRef]())).toMap
  var started = List[FsbtProjectRef]()

  def execute(): Unit = {

      val readyToStart = configs
        .filterNot(x => started.contains(x.self))
        .filter(p => {notifs(p.self).toSet == p.waitFor.toSet})

      readyToStart.par.foreach(x => {
        this.synchronized{
          started = started :+ x.self
        }
        val self = modules.filter(_.projectName == x.self).head
        task.perform(self, taskCompleted)
      })
  }

  def taskCompleted(module: FsbtModule): Unit = {
//    this.synchronized {
      configs.filter(_.self == module.projectName).head.notifyOnComplete.foreach{ projectToNotify =>
        notifs = notifs.updated(projectToNotify, notifs(projectToNotify) :+ module.projectName)
        println(s"Notified $projectToNotify of ${module.projectName} completion")
      }

//    }
    execute()
  }
}
