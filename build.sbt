enablePlugins(SwaggerPlugin)
enablePlugins(PlayScala)

lazy val root = (project in file(".")).settings(
    inThisBuild(List(
      organization := "io.github.0um",
      scalaVersion := "2.12.8",
      version      := "0.1.3-SNAPSHOT"
    )),
    name := "thehand",
    libraryDependencies ++= Seq(
      guice,
      "org.tmatesoft.svnkit" % "svnkit" % "1.9.3",
      "commons-codec" % "commons-codec" % "1.12",
      "org.postgresql" % "postgresql" % "42.2.6",
      "com.h2database" % "h2" % "1.4.198",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.1",
      "joda-time" % "joda-time" % "2.10.2",
      "org.joda" % "joda-convert" % "2.2.1",
      "com.github.tototoshi" %% "scala-csv" % "1.3.5",
      "com.typesafe" % "config" % "1.3.4",
      "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.9.2",
      "com.typesafe.slick" %% "slick" % "3.3.2",
      "com.typesafe.play" % "play-json_2.12" % "2.7.4",
      "com.typesafe.play" %% "play-slick" % "4.0.0",
      "com.typesafe.play" %% "play-json-joda" % "2.7.4",
      specs2 % Test,
      "org.specs2" %% "specs2-matcher-extra" % "4.4.1" % Test,
      "org.webjars" % "swagger-ui" % "2.2.10-1"
    )
  )

routesImport += "models.DatabaseSuffix"
routesImport += "models.QueryMagic"
routesImport += "models.QueryLocalDate"

swaggerDomainNameSpaces := Seq("io.github.0um.models")
swaggerPrettyJson := true

scalacOptions ++= Seq(                 // for scala 2.12
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
  "-Xfuture",                          // Turn on future language features.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
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
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification",             // Enable partial unification in type constructor inference
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened// ]
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
// error
  // "-Ywarn-unused:implicits",          // Warn if an implicit parameter is unused.
  //"-Ywarn-unused:params",              // Warn if a value parameter is unused.
)