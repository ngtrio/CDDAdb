lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "CDDAdb",
    version := "0.1",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
      "org.scalatest" %% "scalatest" % "3.2.0" % "test",
      "org.codehaus.groovy" % "groovy" % "3.0.4"
    )
  )