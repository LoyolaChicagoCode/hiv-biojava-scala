import com.typesafe.sbt.SbtStartScript

import AssemblyKeys._

name := "hiv-biojava-scala"

version := "0.0.2"

scalaVersion := "2.11.7"

resolvers += "BioJava repository" at "http://www.biojava.org/download/maven/"

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "latest.release" % "test",
  "org.biojava" % "core" % "1.8.4",
  "org.biojava" % "alignment" % "1.8.4"
)

seq(SbtStartScript.startScriptForClassesSettings: _*)

assemblySettings
