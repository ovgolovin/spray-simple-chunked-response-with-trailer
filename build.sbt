name := "chunkedResponse"

version := "1.0"

scalaVersion := "2.10.2"


libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.3",
    "io.spray" % "spray-can" % "1.3.1",
    "io.spray" % "spray-routing" % "1.3.1"
  )
}
    