name := "fsbt"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++=
  Seq(
    "org.scala-lang" % "scala-compiler" % "2.11.8",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5",
    "com.github.pathikrit" %% "better-files" % "2.17.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.6",
    "org.scala-sbt" %% "zinc" % "1.0.0-X10",
    "ch.qos.logback" % "logback-classic" % "1.1.7"
  )

unmanagedJars in Compile += file("lib/nailgun-server-0.9.2-SNAPSHOT.jar")


