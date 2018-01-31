package core.config.compile

import java.io

import core.config.FsbtModule
import core.config.FsbtModule.FsbtProjectRef

import scala.collection.{immutable, mutable}

case class ExecutionConfig(self: FsbtProjectRef, notifyOnComplete: List[FsbtProjectRef], waitFor: List[FsbtProjectRef])


object CompileConfig {

  private def getNotifyTargets(modules: List[FsbtModule]): List[(FsbtProjectRef, FsbtProjectRef)] = {
    val a = modules.map(x => (x, x.modules)).flatMap {
      case (module, y) => y.map(xx => (xx, module.projectName))
    }
    val b = modules.map(x => (x.dependsOn, x)).flatMap {
      case (y, module) => y.map(xx => (module.projectName, xx))
    }
    a ::: b
  }

  def build(modules: List[FsbtModule]) = {

    val modulesDeps = getNotifyTargets(modules)
    val dependsOn = modulesDeps.map { case (x, y) => (y, x) }

    modules.map(module =>
      ExecutionConfig(
        module.projectName,
        modulesDeps.filter(_._1 == module.projectName).map(_._2),
        dependsOn.filter(_._1 == module.projectName).map(_._2))
    )
  }
}
