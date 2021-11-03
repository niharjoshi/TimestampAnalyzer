name := "TimestampAnalyzer"

version := "1.0"

scalaVersion := "2.13.4"

lazy val akkaVersion = "2.6.17"
lazy val akkaHttpVersion = "10.2.7"
lazy val akkaGrpcVersion = "2.1.0"

enablePlugins(AkkaGrpcPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http2-support" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-pki" % akkaVersion,

  // The Akka HTTP overwrites are required because Akka-gRPC depends on 10.1.x
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http2-support" % akkaHttpVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.6",

  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,

  "io.spray" %%  "spray-json" % "1.3.6",
  "org.scalaj" % "scalaj-http_2.11" % "2.4.2"
)