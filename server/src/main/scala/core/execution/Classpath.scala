package core.execution

import java.io.File

import core.dependencies.MavenDependency

class Classpath(val targets: List[File],
                 val dependencies: List[MavenDependency]
                 ) {


}
