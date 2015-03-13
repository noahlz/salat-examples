name := "salat-examples"

organization := "com.novus.salat.examples"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4", "2.11.2")

libraryDependencies ++= Seq(
  "com.novus" %% "salat" % "1.9.9", 
  "org.scalatest" %% "scalatest" % "2.2.1" % "test,it"
)

Defaults.itSettings

lazy val root = project.in(file(".")).configs(IntegrationTest)

initialCommands := "import com.novus.salat.examples._"
