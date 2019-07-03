import com.lightbend.lagom.sbt.LagomImport
import sbt.project
import LagomPlugin.autoImport._
import sbt.Keys.javacOptions

organization in ThisBuild := "com.redelastic"

scalaVersion in ThisBuild := "2.12.4"

lagomServiceLocatorEnabled in ThisBuild := true
lagomServiceLocatorPort in ThisBuild := 9108
lagomServiceGatewayPort in ThisBuild := 9109
lagomCassandraCleanOnStart in ThisBuild := true

EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

val hamcrest = "org.hamcrest" % "hamcrest-all" % "1.3" % Test
val junit = "com.novocode" % "junit-interface" % "0.11" % Test

lazy val root = (project in file("."))
  .settings(name := "reactive-stock-trader")
  .aggregate(
    portfolioApi,
    portfolioImpl,
    brokerApi,
    brokerImpl,
    wireTransferApi,
    wireTransferImpl,
    bff
  )
  .settings(commonSettings)

lazy val commonModels = (project in file("common-models"))
  .settings(commonSettings)
  .settings(
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok)
  )

lazy val portfolioApi = (project in file("portfolio-api"))
  .settings(commonSettings)
  .settings(
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= lagomApiDependencies
  ).dependsOn(commonModels)

lazy val portfolioImpl = (project in file("portfolio-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava)
  .settings(
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomJavadslKafkaBroker,
      cassandraExtras
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(
    utils,
    portfolioApi,
    brokerApi,
    wireTransferApi
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(lagomServiceHttpPort := 9202)
  .settings(dockerBaseImage := "openjdk:8-slim")    

lazy val brokerApi = (project in file("broker-api"))
  .settings(commonSettings)
  .settings(
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= lagomApiDependencies
  ).dependsOn(commonModels)


lazy val brokerImpl = (project in file("broker-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava)
  .dependsOn(
    utils,
    brokerApi,
    portfolioApi
  )
  .settings(
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomJavadslKafkaBroker
    ),
    maxErrors := 10000
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(lagomServiceHttpPort := 9201)
  .settings(dockerBaseImage := "openjdk:8-slim")    

lazy val wireTransferApi = (project in file("wire-transfer-api"))
  .settings(commonSettings)
  .settings(
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= lagomApiDependencies
  )
  .dependsOn(portfolioApi)

lazy val wireTransferImpl = (project in file("wire-transfer-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava)
  .dependsOn(
    wireTransferApi
  )
  .settings(
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomJavadslKafkaBroker,
      lagomJavadslPubSub
    ),
    maxErrors := 10000
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(lagomServiceHttpPort := 9200)
  .settings(dockerBaseImage := "openjdk:8-slim")  

lazy val bff = (project in file("bff"))
  .settings(commonSettings)
  .enablePlugins(PlayJava, LagomPlay)
  .disablePlugins(PlayLayoutPlugin)
  .dependsOn(
    utils,
    portfolioApi,
    brokerApi,
    wireTransferApi
  )
  .settings(
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslClient
    ),

    PlayKeys.playMonitoredFiles ++= (sourceDirectories in(Compile, TwirlKeys.compileTemplates)).value,

    // EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
    EclipseKeys.preTasks := Seq(compile in Compile)
  )
  .settings(lagomServiceHttpPort := 9100)
  .settings(dockerBaseImage := "openjdk:8-slim")

lazy val utils = (project in file("utils"))
  .settings(commonSettings)
  .settings(
    version := "0.1-SNAPSHOT"
  )

val lombok = "org.projectlombok" % "lombok" % "1.18.4"
val cassandraExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0"

val lagomApiDependencies = Seq(
  lagomJavadslApi,
  lombok)

def commonSettings: Seq[Setting[_]] = eclipseSettings ++ Seq(
  javacOptions in Compile ++= Seq("-encoding", "UTF-8", "-source", "1.8"),
  javacOptions in(Compile, compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-parameters"),
  libraryDependencies ++= Seq(
    hamcrest,
    junit
  ),
  crossPaths := false // Work around JUnit issue with SBT
)

lagomCassandraCleanOnStart in ThisBuild := true
lagomKafkaCleanOnStart in ThisBuild := true