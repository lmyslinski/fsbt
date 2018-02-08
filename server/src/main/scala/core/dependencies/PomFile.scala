package core.dependencies

case class PomFile(
                    dependencies: List[MavenDependency],
                    properties: Map[String, String],
                    parentPom: Option[PomFile] = None,
                    dependencyManagement: Map[(String, String), MavenDependency] = Map()
                  )

