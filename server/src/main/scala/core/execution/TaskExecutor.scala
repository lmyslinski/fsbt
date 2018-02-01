package core.execution

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
        .filterNot(x => started.contains(x.module))
        .filter(p => {notifs(p.module).toSet == p.waitFor.toSet})

      readyToStart.par.foreach(executionConfig => {
        this.synchronized{
          started = started :+ executionConfig.module
        }
        val self = modules.filter(_.projectName == executionConfig.module).head
        task.perform(self, executionConfig, taskCompleted)
      })
  }

  def taskCompleted(module: FsbtModule): Unit = {
      configs.filter(_.module == module.projectName).head.notifyOnComplete.foreach{ projectToNotify =>
        this.synchronized{
          notifs = notifs.updated(projectToNotify, notifs(projectToNotify) :+ module.projectName)
        }
//        println(s"Notified $projectToNotify of ${module.projectName} completion")
      }
    execute()
  }
}
