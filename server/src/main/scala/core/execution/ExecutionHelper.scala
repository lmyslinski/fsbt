package core.execution

import core.config.FsbtExceptions.ModuleNotFoundException
import core.config.FsbtModule
import core.config.FsbtModule.FsbtProjectRef
import core.config.compile.ExecutionConfig
import core.dependencies.MavenDependencyScope

import scala.collection.immutable.Nil

object ExecutionHelper {

  private def getModuleDeps(modules: List[FsbtModule]): List[(FsbtModule, FsbtProjectRef)] = {
      modules.map(x => (x, x.dependsOn)).flatMap{
        case (module, y) => y.map(xx => (module, xx))
      }
  }

//  def validateModulesPresent(modules: List[FsbtModule]) = {
//
//    val moduleNames = modules.map(_.projectName)
//
//    val moduleDeps: List[(FsbtModule, FsbtProjectRef)] = getModuleDeps(modules)
//
//    val notFound = moduleDeps.filterNot(p => moduleNames.contains(p._2))
//    if(notFound.nonEmpty){
//      throw new ModuleNotFoundException(notFound)
//    }
//  }

  private def getNotifyTargets(modules: List[FsbtModule]): List[(FsbtProjectRef, FsbtProjectRef)] = {
    val a = modules.map(x => (x, x.modules)).flatMap {
      case (module, y) => y.map(xx => (xx, module.projectName))
    }
    val b = modules.map(x => (x.dependsOn, x)).flatMap {
      case (y, module) => y.map(xx => (xx, module.projectName))
    }
    a ::: b
  }

  private def getDependencies(config: FsbtModule, scope: MavenDependencyScope.Value) = config.dependencies.filter(_.scope == scope).map(_.jarFile.toJava)

  private def makeClasspath(module: FsbtModule, modules: List[FsbtModule], moduleDeps: List[(FsbtProjectRef, FsbtProjectRef)]): Classpath = {

    def getNestedDependencies(start: List[FsbtProjectRef], acc: List[FsbtProjectRef] = List()): List[FsbtProjectRef] = {
      start.flatMap(p => {
        moduleDeps.filter(_._2 == p).map(_._1) match {
          case Nil => acc
          case x => getNestedDependencies(x, x ::: acc)
        }
      }).distinct
    }

    def getModule(projectRef: FsbtProjectRef) = modules.filter(_.projectName == projectRef).head



    val nested = getNestedDependencies(List(module.projectName)).map(getModule)

    val compile =
      module.target.toJava :: getDependencies(module, MavenDependencyScope.Compile) :::
        nested.flatMap(x => x.target.toJava :: getDependencies(x, MavenDependencyScope.Compile))

    val test =
      module.target.toJava :: getDependencies(module, MavenDependencyScope.Test) :::
        nested.flatMap(x => x.target.toJava :: getDependencies(x, MavenDependencyScope.Test))

    Classpath(compile.toArray, test.toArray)
  }

  def build(modules: List[FsbtModule]): List[ExecutionConfig] = {

    val modulesDeps = getNotifyTargets(modules)
    val dependsOn = modulesDeps.map { case (x, y) => (y, x) }


    modules.map(module =>
      ExecutionConfig(
        module.projectName,
        makeClasspath(module, modules, modulesDeps),
        modulesDeps.filter(_._1 == module.projectName).map(_._2),
        dependsOn.filter(_._1 == module.projectName).map(_._2))
    )
  }
}
