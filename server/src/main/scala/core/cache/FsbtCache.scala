package core.cache

import java.io._
import java.util.Optional

import better.files.File
import com.twitter.chill.{Externalizer, Input, Output, ScalaKryoInstantiator}
import com.typesafe.scalalogging.Logger
import core.config.FsbtConfig
import core.config.FsbtConfig.fsbtPath
import org.slf4j.LoggerFactory
import xsbti.compile.{CompileResult, PreviousResult}

import scala.collection.immutable.HashMap

object FsbtCache{

  private val zincCache = s"$fsbtPath/cache/compileCache"

  private var localCache : HashMap[String, CompileResult] = new HashMap()

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  private val cacheSerializer = Externalizer(localCache)

  private val kryo = {
    val instantiator = new ScalaKryoInstantiator
    instantiator.setRegistrationRequired(false)
    instantiator.newKryo()
  }

  def loadCache(): Unit = {
    if(File(zincCache).exists){
      logger.debug("Loading cache...")
      try{
        val fis = new FileInputStream(zincCache)
        cacheSerializer.read(kryo, new Input(fis))
        fis.close()
        val cache = cacheSerializer.get
        localCache ++= cache
      }catch{
        case ex: Exception => logger.error("WTF", ex)
      }

    }
    logger.debug(s"Cache: $localCache")
  }

  def updateCache(config: FsbtConfig, cr: CompileResult): Unit = {
    localCache = localCache.updated(config.projectName, cr)
    val fstream = new FileOutputStream(zincCache)
    Externalizer(localCache).write(kryo, new Output(fstream))
    fstream.close()
    logger.debug("Updated cache...")
  }

  def getCompileResult(config: FsbtConfig): PreviousResult = {
    if(localCache.contains(config.projectName)){
      val cache = localCache(config.projectName)
      PreviousResult.create(Optional.of(cache.analysis()), Optional.of(cache.setup()))
    }else{
      PreviousResult.create(Optional.empty(), Optional.empty())
    }
  }
}
