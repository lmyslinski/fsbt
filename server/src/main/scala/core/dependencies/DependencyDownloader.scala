package core.dependencies

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}

import better.files.File
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

object DependencyDownloader {

  def resolveAll(dependencies: List[MavenDependency])(implicit logger: Logger): Unit ={
    dependencies.foreach(downloadJar)
  }

  def downloadJar(dependency: MavenDependency)(implicit logger: Logger): Unit ={

    if(dependency.jarFile.notExists){
      try{
        dependency.jarFile.createDirectories().createIfNotExists()
//        logger.debug(s"Downloading ${dependency.jarUrl}")
        val jarWebsite = new URL(dependency.jarUrl)
        val in2 = jarWebsite.openConnection().getInputStream
        Files.copy(in2, dependency.jarFile.path, StandardCopyOption.REPLACE_EXISTING)
      }catch {
        case ex: Exception => {
          logger.debug(s"Could not download ${dependency.jarUrl}: ${ex}")
          logger.debug(s"Cause: ${ex}")
        }
      }
    }
  }


}
