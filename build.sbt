/*
  General Scala attributes
 */
scalaVersion := "2.12.5"

/*
  General project attributes
 */
organization := "uk.gov.ons"
name := "ONS OpenID Server"
version := "0.1"
description := "A demo how to use OpenID from a REST service"
organizationHomepage := Some(url("http://www.ons.gov.uk"))

/*
  Project dependencies
 */
libraryDependencies ++= Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % "1.5.3.RELEASE",
  "org.springframework.boot" % "spring-boot-configuration-processor" % "1.5.3.RELEASE",
  "org.springframework.boot" % "spring-boot-starter-actuator" % "1.5.3.RELEASE",
  "org.springframework.boot" % "spring-boot-starter-security" % "1.5.3.RELEASE",
  "org.springframework.boot" % "spring-boot-configuration-processor" % "1.5.3.RELEASE" % "compile",
  "org.springframework.security.oauth" % "spring-security-oauth2" % "2.3.2.RELEASE",
  "org.keycloak" % "keycloak-spring-boot-starter" % "3.1.0.Final",
  "org.keycloak.bom" % "keycloak-adapter-bom" % "3.1.0.Final" pomOnly()
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