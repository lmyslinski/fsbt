package core.config.compile

import core.config.FsbtModule
import core.config.FsbtModule.FsbtProjectRef
import core.dependencies.MavenDependencyScope
import core.execution.Classpath

import scala.collection.immutable.Nil

case class ExecutionConfig(module: FsbtProjectRef, classpath: Classpath, notifyOnComplete: List[FsbtProjectRef], waitFor: List[FsbtProjectRef])


object CompileConfig {


}
