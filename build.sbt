name := "AgodaDownloader"

version := "0.1"

scalaVersion := "2.13.2"

organization := "com.agoda.downloader"

libraryDependencies ++= {
  val akkaVersion = "2.6.8"
  val akkaHttp = "10.2.4"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttp,
    "com.typesafe.akka" %% "akka-http" % akkaHttp,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.play" %% "play-ws-standalone-json" % "2.1.2",
    "de.heikoseeberger" %% "akka-http-play-json" % "1.33.0",
    "org.slf4j" % "slf4j-api" % "1.7.5",
    "org.slf4j" % "slf4j-simple" % "1.7.5",
    "com.jcraft" % "jsch" % "0.1.55",
    "com.h2database" % "h2" % "1.4.197",

    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.1.2" % Test
  )
}