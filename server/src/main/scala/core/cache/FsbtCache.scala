package core.cache

import java.io._
import java.util.Optional

import better.files.File
import com.typesafe.scalalogging.Logger
import core.config.FsbtConfig
import core.config.FsbtConfig.fsbtPath
import org.slf4j.LoggerFactory
import com.twitter.chill.{Externalizer, Input, MeatLocker, Output, ScalaKryoInstantiator}
import com.twitter.chill
import xsbti.compile.{CompileResult, PreviousResult}

import scala.collection.concurrent.TrieMap

object FsbtCache{

  private val zincCache = s"$fsbtPath/cache/compileCache"

  private val localCache : TrieMap[String, CompileResult] = TrieMap()

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  private val cacheSerializer = Externalizer(localCache)

  private val kryo = {
    val instantiator = new ScalaKryoInstantiator
    instantiator.setRegistrationRequired(false)
    instantiator.newKryo()
  }

  def loadCache(): Unit = {
    if(File(zincCache).exists){
      cacheSerializer.read(kryo, new Input(new FileInputStream(zincCache)))
      val cache = cacheSerializer.get
      localCache ++= cache
    }
  }

  def updateCache(config: FsbtConfig, cr: CompileResult): Unit = {
    localCache.put(config.projectName, cr)
    cacheSerializer.write(kryo, new Output(new FileOutputStream(zincCache)))
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
