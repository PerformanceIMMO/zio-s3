import BuildHelper._

inThisBuild(
  List(
    organization := "performance.immo",
    homepage := Some(url("https://zio.dev/zio-s3/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("regis-leray", "Regis Leray", "regis.leray@gmail.com", url("https://github.com/regis-leray"))
    ),
    Test / fork := true,
    (Test / parallelExecution) := false,
    scmInfo := Some(
      ScmInfo(url("https://github.com/zio/zio-s3/"), "scm:git:git@github.com:zio/zio-s3.git")
    ),
    credentials += Credentials(
      "Sonatype Nexus Repository Manager",
      "team.performance.immo",
      sys.env.getOrElse("NEXUS_USER", ""),
      sys.env.getOrElse("NEXUS_PWD", "")
    )
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

val zioVersion  = "2.0.21"
val awsVersion  = "2.25.27"
val sttpVersion = "3.9.7"

lazy val root =
  project.in(file(".")).settings(publish / skip := true).aggregate(`zio-s3`, docs)

lazy val `zio-s3` = project
  .in(file("zio-s3"))
  .enablePlugins(BuildInfoPlugin)
  .settings(buildInfoSettings("zio.s3"))
  .settings(stdSettings("zio-s3"))
  .settings(dottySettings)
  .settings(publishTo := {
    val nexus = "https://team.performance.immo/nexus/repository"

    if (isSnapshot.value) Some("snapshots".at(nexus + "/maven-snapshots/"))
    else Some("releases".at(nexus + "/maven-releases/"))
  })
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"                       %% "zio"                         % zioVersion,
      "dev.zio"                       %% "zio-streams"                 % zioVersion,
      "dev.zio"                       %% "zio-nio"                     % "2.0.2",
      "dev.zio"                       %% "zio-interop-reactivestreams" % "2.0.2",
      "software.amazon.awssdk"         % "s3"                          % awsVersion,
      "software.amazon.awssdk"         % "sts"                         % awsVersion,
      "dev.zio"                       %% "zio-test"                    % zioVersion  % Test,
      "dev.zio"                       %% "zio-test-sbt"                % zioVersion  % Test,
      "com.dimafeng"                  %% "testcontainers-scala-minio"  % "0.41.4"    % Test,
      "com.softwaremill.sttp.client3" %% "core"                        % sttpVersion % Test,
      "com.softwaremill.sttp.client3" %% "zio"                         % sttpVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val docs = project
  .in(file("zio-s3-docs"))
  .settings(stdSettings("zio-s3-docs"))
  .settings(
    moduleName := "zio-s3-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    projectName := "ZIO S3",
    mainModuleName := (`zio-s3` / moduleName).value,
    projectStage := ProjectStage.ProductionReady,
    docsPublishBranch := "series/2.x",
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(`zio-s3`),
    //conflict with the dependency zio-nio & sbt-mdoc
    excludeDependencies += "org.scala-lang.modules" % "scala-collection-compat_3",
    publish / skip := true
  )
  .dependsOn(`zio-s3`)
  .enablePlugins(WebsitePlugin)
