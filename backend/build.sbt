name := "fsbt"

version := "0.0.1"

scalaVersion := "2.12.2"

libraryDependencies ++=
  Seq(
    "org.scala-sbt" %% "zinc" % "1.0.0-X20",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5",
    "com.github.pathikrit" %% "better-files" % "2.17.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.1.7"
  )

unmanagedJars in Compile += file("lib/nailgun-server-0.9.2-SNAPSHOT.jar")


