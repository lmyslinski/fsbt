package core.execution

import ch.qos.logback.classic.Logger
import com.martiansoftware.nailgun.NGContext
import core.config.FsbtModule
import core.config.FsbtModule.FsbtProjectRef
import core.config.compile.ExecutionConfig
import core.execution.tasks._

class TaskExecutor(val modules: List[FsbtModule], val configs: List[ExecutionConfig],val task: Task)(implicit ctx: NGContext, logger: Logger) {

  private var notifs = modules.map(x => (x.projectName, List[FsbtProjectRef]())).toMap
  private var started = List[FsbtProjectRef]()

  def execute(): Unit = {
    task match {
      case Compile() => executeInParallel()
      case Run() => executeOneOff()
      case Clean() => executeForAll()
      case Stop() => executeOneOff()
      case Test() => executeForAll()
    }
  }

  private def executeOneOff(): Unit = {
    val rootModule = modules.find(_.isRootProject).get
    val rootConfig = configs.find(p => rootModule.projectName == p.module).head
    task.perform(rootModule, rootConfig, (module: FsbtModule) => {
      println("Complete")
    })
  }

  private def executeForAll(): Unit = {
    val rootModule = modules.find(_.isRootProject).get
    val rootConfig = configs.find(p => rootModule.projectName == p.module).head
    task.perform(rootModule, rootConfig, (module: FsbtModule) => {
      println("Complete")
    })
  }

  private def executeInParallel(): Unit = {
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

  private def taskCompleted(module: FsbtModule): Unit = {
      configs.filter(_.module == module.projectName).head.notifyOnComplete.foreach{ projectToNotify =>
        this.synchronized{
          notifs = notifs.updated(projectToNotify, notifs(projectToNotify) :+ module.projectName)
        }
      }
    execute()
  }
}
