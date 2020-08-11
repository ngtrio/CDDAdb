lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "CDDAdb",
    version := "0.1",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      guice,
      ws,
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
      "org.scalatest" %% "scalatest" % "3.2.0" % "test",
      "org.codehaus.groovy" % "groovy" % "3.0.4",
      "org.jsoup" % "jsoup" % "1.13.1",
      "com.github.pathikrit" %% "better-files" % "3.9.1"
    )
  )