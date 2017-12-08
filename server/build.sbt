//maintainer := "Humblehound <lukmyslinski@gmail.com>"
//packageSummary := "fsbt Debian Package"

libraryDependencies ++=
  Seq(
    "org.scala-sbt" %% "zinc" % "1.0.0",
    "org.scala-sbt" %% "io" % "1.1.0",
    "org.scala-sbt" %% "util-logging" % "1.0.2",
    "org.scala-sbt" %% "compiler-bridge" % "1.0.0",
    "org.scala-sbt" % "compiler-interface" % "1.0.0",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5",
    "com.github.pathikrit" %% "better-files" % "2.17.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    "com.google.guava" % "guava" % "23.0",
    "com.google.code.findbugs" % "jsr305" % "3.0.2",
    "com.martiansoftware" % "nailgun-server" % "0.9.1",
    "com.twitter" %% "chill" % "0.9.2"
  )

mainClass in Compile := Some("com.martiansoftware.nailgun.NGServer")

//enablePlugins(JavaAppPackaging)
//enablePlugins(DebianPlugin)