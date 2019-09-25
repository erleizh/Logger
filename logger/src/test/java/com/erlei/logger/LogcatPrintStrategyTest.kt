package com.erlei.logger

import com.erlei.logger.Logger.DEBUG
import com.erlei.logger.printer.LogcatPrintStrategy
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class LogcatPrintStrategyTest {

  @Test
  fun log() {
    val logStrategy = LogcatPrintStrategy()

    logStrategy.print(LogLine.obtain(DEBUG, "tag", "message"))

    val logItems = ShadowLog.getLogs()
    assertThat(logItems[0].type).isEqualTo(DEBUG)
    assertThat(logItems[0].msg).isEqualTo("message")
    assertThat(logItems[0].tag).isEqualTo("tag")
  }

  @Test
  fun logWithNullTag() {
    val logStrategy = LogcatPrintStrategy()

    logStrategy.print(LogLine.obtain(DEBUG, null, "message"))

    val logItems = ShadowLog.getLogs()
    assertThat(logItems[0].tag).isEqualTo("")
  }

}
