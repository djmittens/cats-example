lazy val root = (project in file(".")).settings(
  name := "cats-example",

  //Partial unification should be on to improve type inference
  scalacOptions += "-Ypartial-unification",
  libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0",
  libraryDependencies += "org.typelevel" %% "cats-kernel" % "1.5.0",
  libraryDependencies += "org.typelevel" %% "cats-free" % "1.5.0",
  libraryDependencies += "org.typelevel" %% "alleycats-core" % "1.5.0",
  libraryDependencies += "org.typelevel" %% "cats-effect" % "1.1.0",

  //Maybe some amazon - ish types of  stuff goes in here ?
  libraryDependencies += "org.tpolecat" %% "doobie-core" % "0.6.0",
  libraryDependencies += "org.tpolecat" %% "doobie-hikari"  % "0.6.0",
  libraryDependencies += "org.tpolecat" %% "doobie-scalatest" % "0.6.0" % "test",  // ScalaTest support for typechecking statements.

  //Sqlite Database
  libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.23.1",
)
