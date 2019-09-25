package com.erlei.logger

import com.erlei.logger.format.CsvFormatStrategy
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CsvFormatStrategyTest {

  @Test
  fun log() {
    val formatStrategy = CsvFormatStrategy.newBuilder()
        .logStrategy {
          assertThat(it.fulTag).isEqualTo("PRETTY_LOGGER-tag")
          assertThat(it.level).isEqualTo(Logger.VERBOSE)
          assertThat(it.message).contains("VERBOSE,PRETTY_LOGGER-tag,message")
        }
        .build()

    formatStrategy.log(LogLine.obtain(Logger.VERBOSE, "tag", "message"))
  }

  @Test
  fun defaultTag() {
    val formatStrategy = CsvFormatStrategy.newBuilder()
        .logStrategy {
          assertThat(it.fulTag).isEqualTo("PRETTY_LOGGER")
        }
        .build()

    formatStrategy.log(LogLine.obtain(Logger.VERBOSE, null, "message"))
  }

  @Test
  fun customTag() {
    val formatStrategy = CsvFormatStrategy.newBuilder()
        .tag("custom")
        .logStrategy {
          assertThat(it.fulTag).isEqualTo("custom")
        }
        .build()

    formatStrategy.log(LogLine.obtain(Logger.VERBOSE, null, "message"))
  }
}
