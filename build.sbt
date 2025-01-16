import org.scalajs.linker.interface.ModuleInitializer
lazy val LineDancing2 = crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Full).in(file("."))
  .settings(
    name := "LineDancing2",
    version := "0.1-SNAPSHOT",
    scalaVersion := "3.3.4",
    libraryDependencies ++= Seq(
      "org.virtuslab" %%% "scala-yaml" % "0.0.7"
    ),
    Compile / mainClass := Some("main")
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "org.rogach" %% "scallop" % "4.1.0"
    ),
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.5.0",
    scalaJSUseMainModuleInitializer := true
  )
