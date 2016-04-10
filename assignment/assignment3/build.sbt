name := """Playdemo"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,



  specs2 % Test
)
libraryDependencies ++= Seq("org.reactivemongo" %% "play2-reactivemongo" % "0.11.11-play24", "org.webjars" %% "webjars-play" % "2.4.0-1",
"org.webjars" % "bootstrap" % "3.1.1-2" )


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"


libraryDependencies ++= Seq(
  "com.adrianhurt" %% "play-bootstrap" % "1.0-P24-B3-SNAPSHOT"
)
offline := true

