name := "freedoobie"

scalaVersion := "2.11.8"

scalaOrganization := "org.typelevel"

version := "0.1"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies in ThisBuild ++= List(
  "com.chuusai"    %% "shapeless"        % "2.3.1",
  "org.typelevel"  %% "cats"             % "0.7.2",
  "org.tpolecat"   %% "doobie-core-cats" % "0.3.1-SNAPSHOT",
  "org.postgresql"  % "postgresql"       % "9.4.1211"
)

scalacOptions in ThisBuild ++= List(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ypartial-unification"
)

scalacOptions in Test ++= Seq("-Yrangepos")

cancelable in Global := true

fork in run := true

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.0")