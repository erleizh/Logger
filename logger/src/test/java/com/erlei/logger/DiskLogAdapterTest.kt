package com.erlei.logger

import com.erlei.logger.adapter.DiskLogAdapter
import com.erlei.logger.format.FormatStrategy
import com.google.common.truth.Truth.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks

class DiskLogAdapterTest {

  @Mock private lateinit var formatStrategy: FormatStrategy

  @Before fun setup() {
    initMocks(this)
  }

  @Test fun isLoggableTrue() {
    val logAdapter = DiskLogAdapter(formatStrategy)

    assertThat(logAdapter.isLoggable(LogLine.obtain(Logger.ERROR, "tag"))).isTrue()
  }

  @Test fun isLoggableFalse() {
    val logAdapter = object : DiskLogAdapter(formatStrategy) {
      override fun isLoggable(logLine: LogLine): Boolean {
        return false
      }
    }

    assertThat(logAdapter.isLoggable(LogLine.obtain(Logger.VERBOSE, "tag"))).isFalse()
  }

  @Test fun log() {
    val logAdapter = DiskLogAdapter(formatStrategy)

    logAdapter.log(LogLine.obtain(Logger.ERROR, "tag", "message"))


    verify {
      Assert.assertEquals(Logger.ERROR, it.level)
      Assert.assertEquals("tag", it.fulTag)
      Assert.assertEquals("message", it.message)
    }

  }

  private fun verify(function: (LogLine) -> Unit) {
    val captor = ArgumentCaptor.forClass(LogLine::class.java)
    verify(formatStrategy).log(captor.capture())
    val value = captor.value
    function.invoke(value)
  }

}
