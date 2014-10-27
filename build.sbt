name := "cube-core"

scalaVersion :="2.11.2"

version :="0.1"

logLevel in Global := Level.Error

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "com.almworks.sqlite4java" % "sqlite4java" % "0.282",
  "com.almworks.sqlite4java" % "libsqlite4java-osx" % "0.282"
)
