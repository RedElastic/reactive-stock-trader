

organization in ThisBuild := "com.redelastic"

scalaVersion in ThisBuild := "2.12.4"

EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

lazy val root = (project in file("."))
  .settings(name := "reactive-stock-trader")
  .aggregate(
    portfolioApi,
    portfolioImpl,
    brokerApi,
    brokerImpl,
    wireTransferApi,
    bff,
    wireTransferImpl
  )
  .settings(commonSettings)

lazy val commonModels = (project in file("commonModels"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok)
  )

lazy val portfolioApi = (project in file("portfolio-api"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= lagomApiDependencies
  ).dependsOn(commonModels)

lazy val portfolioImpl = (project in file("portfolio-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .settings(
    version := "1.0-SNAPSHOT",
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

lazy val brokerApi = (project in file("broker-api"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= lagomApiDependencies
  ).dependsOn(commonModels)


lazy val brokerImpl = (project in file("broker-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .dependsOn(
    utils,
    brokerApi,
    portfolioApi
  )
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomJavadslKafkaBroker
    ),
    maxErrors := 10000
  )

lazy val wireTransferApi = (project in file("wire-transfer-api"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= lagomApiDependencies
  )
  .dependsOn(portfolioApi)

lazy val wireTransferImpl = (project in file("wire-transfer-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .dependsOn(
    wireTransferApi
  )
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit,
      lagomJavadslKafkaBroker
    ),
    maxErrors := 10000

  )

lazy val bff = (project in file("bff"))
  .settings(commonSettings)
  .enablePlugins(PlayJava, LagomPlay, SbtReactiveAppPlugin)
  .disablePlugins(PlayLayoutPlugin)
  .dependsOn(
    utils,
    portfolioApi,
    brokerApi,
    wireTransferApi
  )
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslClient
    ),

    PlayKeys.playMonitoredFiles ++= (sourceDirectories in(Compile, TwirlKeys.compileTemplates)).value,

    // EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
    EclipseKeys.preTasks := Seq(compile in Compile)
  )

lazy val utils = (project in file("utils"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT"
  )


val lombok = "org.projectlombok" % "lombok" % "1.18.4"
val cassandraExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0"

val lagomApiDependencies = Seq(
  lagomJavadslApi,
  lombok)



def commonSettings: Seq[Setting[_]] = eclipseSettings ++ Seq(
  javacOptions in Compile ++= Seq("-encoding", "UTF-8", "-source", "1.8"),
  javacOptions in(Compile, compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-parameters"),
  libraryDependencies += "org.hamcrest" % "hamcrest-all" % "1.3" % Test,
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
  crossPaths := false // Work around JUnit issue with SBT
)

lagomCassandraCleanOnStart in ThisBuild := true
lagomKafkaCleanOnStart in ThisBuild := true