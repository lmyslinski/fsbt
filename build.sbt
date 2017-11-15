
maintainer := "Humblehound <lukmyslinski@gmail.com>"
packageSummary := "fsbt Debian Package"

lazy val fsbt = (project in file(".")).aggregate(server).dependsOn(server)

lazy val commonSettings = Seq(
    name := "fsbt",
    version := "0.0.1"
)

lazy val server = (project in file("server"))
.settings(
    commonSettings,
    name := "fsbt-server"
)

mainClass in Compile := Some("com.martiansoftware.nailgun.NGServer")

bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / "scripts" / "extra.sh")

enablePlugins(JavaAppPackaging)
enablePlugins(DebianPlugin)

