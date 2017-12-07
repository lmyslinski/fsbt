package core.config

import better.files.File
import core.config.FsbtProject.Variables
import core.dependencies.MavenDependency

case class FsbtModule(dependencies: List[MavenDependency],
                      workDir: String,
                      target: File,
                      moduleName: String,
                      variables: Variables,
                      modules: List[FsbtModule])
