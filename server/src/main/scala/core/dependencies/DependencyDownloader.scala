package core.dependencies

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}

import better.files.File
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object DependencyDownloader {

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def resolveAll(dependencies: List[MavenDependency]): Unit ={
    dependencies.foreach(downloadJar)
  }

  def downloadJar(dependency: MavenDependency): Unit ={

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
