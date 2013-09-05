import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play_demo"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.novus" %% "salat" % "1.9.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += Resolver.url("Violas Play Modules", url("http://www.joergviola.de/releases/"))(Resolver.ivyStylePatterns),
      resolvers += "repo.novus releases" at "http://repo.novus.com/releases/"
    )

}
