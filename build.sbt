enablePlugins(SwaggerPlugin)
enablePlugins(PlayScala)
enablePlugins(JavaServerAppPackaging)
// enablePlugins(WindowsPlugin) uncomment to generate with windows:packegeBin

lazy val root = (project in file(".")).settings(
    inThisBuild(List(
      organization := "io.github.0um",
      scalaVersion := "2.13.1",
      version      := "0.1.7"
    )),
    name := "thehand",
    libraryDependencies ++= Seq(
      guice,
      "org.tmatesoft.svnkit" % "svnkit" % "1.10.1",
      "commons-codec" % "commons-codec" % "1.15",
      "org.postgresql" % "postgresql" % "42.2.17",
      "com.h2database" % "h2" % "1.4.200",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.2",
      "joda-time" % "joda-time" % "2.10.6",
      "org.joda" % "joda-convert" % "2.2.1",
      "com.typesafe" % "config" % "1.4.0",
      "com.typesafe.scala-logging" % "scala-logging_2.13" % "3.9.2",
      "com.typesafe.play" % "play-json_2.13" % "2.9.1",
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "com.typesafe.play" % "play-json_2.13" % "2.9.1",
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.typesafe.play" %% "play-json-joda" % "2.9.1",
      specs2 % Test,
      "org.specs2" %% "specs2-matcher-extra" % "4.10.4" % Test,
      "org.webjars" % "swagger-ui" % "3.25.0"
    )
  )

// general package information (can be scoped to Windows)
maintainer := "Jeison Cardoso <cardoso.jeison@gmail.com>"
packageSummary := "the-hand-windows"
packageDescription := """The Hand Windows MSI."""

// wix build information
wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"
wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"

routesImport += "models.DatabaseSuffix"
routesImport += "models.QueryMagic"
routesImport += "models.QueryLocalDate"

swaggerDomainNameSpaces := Seq("io.github.0um.models")
swaggerPrettyJson := true

scalacOptions ++= Seq(                 // for scala 2.13
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:reflectiveCalls",
  "-language:postfixOps",
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
)

sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false