name := "mockquery"

organization := "jp.sf.amateras.mockquery"

version := "0.0.3"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.12.3" % "provided"
)

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

publishTo := Some(Resolver.ssh("amateras-repo-scp", "shell.sourceforge.jp", "/home/groups/a/am/amateras/htdocs/mvn/") withPermissions("0664")
  as(System.getProperty("user.name"), new java.io.File(Path.userHome.absolutePath + "/.ssh/id_rsa")))
