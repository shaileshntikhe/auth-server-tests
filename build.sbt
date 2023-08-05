version in ThisBuild := "0.1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.10.7"

lazy val root = (project in file("."))
  .settings(
    name := "http-client"
  )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.16" % Test,
  "org.apache.httpcomponents" % "httpclient" % "4.5.14"
)