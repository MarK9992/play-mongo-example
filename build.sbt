name := """testscala"""

version := "1.0-SNAPSHOT"

lazy val playMongoExample = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14-play23"
)
