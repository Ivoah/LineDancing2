lazy val LineDancing2 = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Full).in(file("."))
  .settings(
    name := "LineDancing2",
    version := "0.1-SNAPSHOT",
    scalaVersion := "3.2.2",
    libraryDependencies ++= Seq(
      "org.virtuslab" %%% "scala-yaml" % "0.0.7"
    )
  )
  .jvmSettings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true
  )
