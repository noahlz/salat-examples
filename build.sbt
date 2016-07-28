name := "salat-examples"

organization := "com.novus.salat.examples"

version := "0.4.0-SNAPSHOT"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4", "2.11.2")

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.github.salat" %% "salat" % "1.10.0-SNAPSHOT",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "org.slf4j" % "slf4j-simple" % "1.7.21",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test,it"
)

Defaults.itSettings

lazy val root = project.in(file(".")).configs(IntegrationTest)

initialCommands := "import com.novus.salat.examples._"
