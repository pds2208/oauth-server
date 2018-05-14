import Dependencies._
import sbt.Keys.libraryDependencies

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "gov.ons",
      scalaVersion := "2.12.5",
      version := "0.1.0-SNAPSHOT"
    )),

    name := "ONS OAuth Server",

    libraryDependencies ++= Seq(
      "org.springframework.boot" % "spring-boot-starter-web" % "1.5.3.RELEASE",
      "org.springframework.boot" % "spring-boot-configuration-processor" % "1.5.3.RELEASE",
      "org.springframework.boot" % "spring-boot-starter-actuator" % "1.5.3.RELEASE",
      "org.springframework.boot" % "spring-boot-starter-security" % "1.5.3.RELEASE",
      "org.springframework.boot" % "spring-boot-configuration-processor" % "1.5.3.RELEASE" % "compile",
      "org.springframework.security.oauth" % "spring-security-oauth2" % "2.3.2.RELEASE",
      "org.keycloak" % "keycloak-spring-boot-starter" % "3.1.0.Final",
      "org.keycloak.bom" % "keycloak-adapter-bom" % "3.1.0.Final" pomOnly(),
      scalaTest % Test
    )
  )

/*
  Packaging plugin
 */

// enable the Java app packaging archetype and Ash script (for Alpine Linux, doesn't have Bash)
enablePlugins(JavaAppPackaging, AshScriptPlugin)

// set the main entry point to the application that is used in startup scripts
mainClass in Compile := Some("gov.uk.ons.registers.microservice.MessageServiceApplication")

// the Docker image to base on (alpine is smaller than the debian based one (120 vs 650 MB)
dockerBaseImage := "openjdk:8-jre-alpine"

// creates tag 'latest' as well when publishing
dockerUpdateLatest := true
