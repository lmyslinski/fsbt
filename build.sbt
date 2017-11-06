


lazy val root = (project in file(".")).aggregate(backend).dependsOn(backend)

lazy val commonSettings = Seq(
    name := "fsbt",
    version := "0.0.1"
)

lazy val backend = (project in file("backend"))
.settings(
    commonSettings,
    name := "fsbt-server"
)
