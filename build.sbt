name := """malabar-frontend"""
organization := "online.malabar"

version := "6.0.0"

scalaVersion := "2.13.6"

resolvers += Resolver.jcenterRepo
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"


lazy val playSlickVersion = "4.0.2"
lazy val playSilhouetteVersion = "7.0.2"


libraryDependencies ++= Seq(
  "io.github.honeycomb-cheesecake" %% "play-silhouette" % playSilhouetteVersion,
  "io.github.honeycomb-cheesecake" %% "play-silhouette-password-bcrypt" % playSilhouetteVersion,
  "io.github.honeycomb-cheesecake" %% "play-silhouette-persistence" % playSilhouetteVersion,
  "io.github.honeycomb-cheesecake" %% "play-silhouette-crypto-jca" % playSilhouetteVersion,
  "io.github.honeycomb-cheesecake" %% "play-silhouette-totp" % playSilhouetteVersion,

  "org.postgresql" % "postgresql" % "9.4.1211",
  "com.github.tminglei" %% "slick-pg" % "0.18.0",
  "net.codingwell" %% "scala-guice" % "5.0.2",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.18.0",
  "com.iheart" %% "ficus" % "1.5.1",
  "com.typesafe.play" %% "play-slick"               % playSlickVersion,
  "com.typesafe.play" %% "play-slick-evolutions"    % playSlickVersion,
  ehcache,
  guice,
  filters,

  "com.sendgrid" % "sendgrid-java" % "4.4.1",

  "solutions.iog" %% "psg-cardano-wallet-api" % "0.2.4",
  "com.univocity" % "envlp-cardano-wallet" % "2020.12.21-SNAPSHOT",
  "com.bloxbean.cardano" % "cardano-client-lib" % "0.1.5",
  "com.bloxbean.cardano" % "cardano-client-backend-gql" % "0.1.4",

  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.0",
  "net.debasishg" %% "redisclient" % "3.41",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,

  "com.typesafe.play" %% "play-mailer" % "7.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "7.0.1",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.3-akka-2.6.x",

  "io.github.honeycomb-cheesecake" %% "play-silhouette-testkit" % playSilhouetteVersion % "test",
  specs2 % Test,
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

routesImport += "utils.route.Binders._"

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  //"-Xlint", // Enable recommended additional warnings.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  // Play has a lot of issues with unused imports and unsued params
  // https://github.com/playframework/playframework/issues/6690
  // https://github.com/playframework/twirl/issues/105
  "-Xlint:-unused,_"
)
