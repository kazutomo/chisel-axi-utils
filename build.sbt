// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.18"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.github.kazutomo"
ThisBuild / logLevel := Level.Warn

// Test / parallelExecution := false
Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary
Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

val chiselVersion = "7.9.0"
val scalatestVersion = "3.2.19"

lazy val axichisel = (project in file("."))
  .settings(
    name := "axichisel",
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel" % chiselVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % "test",
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Ymacro-annotations",
    ),
    addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full),
  )
