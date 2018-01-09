package util

import ch.qos.logback.classic.{Logger, LoggerContext}
import com.martiansoftware.nailgun.NGContext
import org.slf4j.LoggerFactory

trait LazyNailLogging {

  protected def getLogger(implicit ctx: NGContext): Logger = {
    val logger = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext].getLogger(getClass)
    logger.addAppender(LoggerUtil.getAppender(ctx.out))
    logger
  }
}
