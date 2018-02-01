package util

import java.io.{OutputStream, PrintStream}

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger, LoggerContext}
import ch.qos.logback.core.OutputStreamAppender
import core.execution.tasks.Compile
import org.slf4j.LoggerFactory

object LoggerUtil {

  def getAppender(out: PrintStream) = {
    val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    // Encoder
    val encoder = new PatternLayoutEncoder
    encoder.setContext(context)
    encoder.setPattern("%d{HH:mm:ss} %-5level - %msg%n")
    encoder.start()

    // OutputStreamAppender
    val appender = new OutputStreamAppender[ILoggingEvent]
    appender.setName("OutputStream Appender")
    appender.setContext(context)
    appender.setEncoder(encoder)
    appender.setOutputStream(out)
    appender.start

    appender
  }
}
