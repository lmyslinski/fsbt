package core.tasks

import core.config.FsbtExceptions.ModuleNotFoundException
import core.config.FsbtModule
import core.config.FsbtModule.FsbtProjectRef

object ExecutionHelper {


//  private def getModules(project: FsbtModule): List[FsbtModule] = {
//    project :: project.modules.flatMap(getModules)
//  }
//
  private def getModuleDeps(modules: List[FsbtModule]): List[(FsbtModule, FsbtProjectRef)] = {
      modules.map(x => (x, x.dependsOn)).flatMap{
        case (module, y) => y.map(xx => (module, xx))
      }
  }

  def stage2(modules: List[FsbtModule]) = {

    val moduleNames = modules.map(_.projectName)

    val moduleDeps: List[(FsbtModule, FsbtProjectRef)] = getModuleDeps(modules)

    val notFound = moduleDeps.filterNot(p => moduleNames.contains(p._2))
    if(notFound.nonEmpty){
      throw new ModuleNotFoundException(notFound)
    }
  }
}
