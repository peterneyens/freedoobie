

lazy val buildSettings = Seq(
  version := "0.1",
  scalaVersion := "2.11.8",
  scalaOrganization := "org.typelevel",
  licenses ++= Seq(("MIT", url("http://opensource.org/licenses/MIT")))
)


lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
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
  ),
  libraryDependencies ++= List(
    "com.chuusai"    %% "shapeless"        % "2.3.1",
    //"org.typelevel"  %% "cats"             % "0.7.2",
    //"org.tpolecat"   %% "doobie-core-cats" % "0.3.1-SNAPSHOT",
    "org.tpolecat"   %% "doobie-core-cats" % "0.3.1-M2"

    //"org.postgresql"  % "postgresql"       % "9.4.1211",
    //"com.googlecode.log4jdbc" % "log4jdbc" % "1.2",
    //"oncue.journal"          %% "core"     % "2.2.1"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.0")
)


lazy val core = project.in(file("core"))
  .settings(
    name := "freedoobie",
    buildSettings ++ commonSettings
  )

/*
lazy val core = project.in(file("modules/core"))
  .settings(
    yax(file("yax/core"), "scalaz"),
    name := "freedoobie",
    buildSettings ++ commonSettings,
    libraryDependencies ++= Seq(
      "org.tpolecat"        %% "doobie-core"       % "0.3.1-M2"
    )
  )

lazy val core_cats = project.in(file("modules-cats/core"))
  .settings(
    yax(file("yax/core"), "cats", "fs2"),
    name := "freedoobie-cats",
    buildSettings ++ commonSettings,
    libraryDependencies ++= Seq(
      "org.tpolecat"        %% "doobie-core-cats"  % "0.3.1-M2"
    )
  )
*/

lazy val example = project.in(file("example"))
  .settings(
    name := "freedoobie-example",
    buildSettings ++ commonSettings,
    libraryDependencies ++= List(
      "org.postgresql"           % "postgresql"  % "9.4.1211",
      "com.googlecode.log4jdbc"  % "log4jdbc"    % "1.2",
      "oncue.journal"           %% "core"        % "2.2.1"
    )
  )
  .dependsOn(core)


cancelable in Global := true

fork in run := true