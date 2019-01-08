organization in ThisBuild := "com.redelastic"

scalaVersion in ThisBuild := "2.12.4"

EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

lazy val root = (project in file("."))
  .settings(name := "reactive-stock-trader")
  .aggregate(
    orderModel,
    portfolioApi,
    portfolioImpl,
    brokerApi,
    brokerImpl,
    wireTransferApi
    //wireTransferImpl
  )
  .settings(commonSettings)

lazy val orderModel = (project in file("order"))
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
  ).dependsOn(orderModel)

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
    portfolioApi,
    brokerApi,
    wireTransferApi
  )

lazy val brokerApi = (project in file("broker-api"))
  .settings(commonSettings)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= lagomApiDependencies
  ).dependsOn(orderModel)


lazy val brokerImpl = (project in file("broker-impl"))
  .settings(commonSettings)
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .dependsOn(brokerApi, portfolioApi)
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
/*
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
*/
lazy val gateway = (project in file("gateway"))
  .settings(commonSettings)
  .enablePlugins(PlayJava, LagomPlay, SbtReactiveAppPlugin)
  .disablePlugins(PlayLayoutPlugin)
  .dependsOn(
    portfolioApi,
    brokerApi
  )
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslClient
    ),

  PlayKeys.playMonitoredFiles ++= (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value,
    
  // EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
  EclipseKeys.preTasks := Seq(compile in Compile)
)

val lombok = "org.projectlombok" % "lombok" % "1.18.4"
val cassandraExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0"

val lagomApiDependencies = Seq(
  lagomJavadslApi,
  lombok)

def commonSettings: Seq[Setting[_]] = eclipseSettings ++ Seq(
  javacOptions in Compile ++= Seq("-encoding", "UTF-8", "-source", "1.8"),
  javacOptions in(Compile, compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-parameters")
)

lagomCassandraCleanOnStart in ThisBuild := false