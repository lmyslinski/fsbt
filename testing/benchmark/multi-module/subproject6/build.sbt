name := "subproject6"

organization := "com.example"

version := "1.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test
)

