package com.erlei.logger

import android.support.test.InstrumentationRegistry.getTargetContext
import com.erlei.logger.Logger.*
import com.erlei.logger.adapter.LogAdapter
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LoggerTest {

  lateinit var logger: Logger
  val logs = arrayListOf<LogLine>()

  @Before
  fun before() {
    logs.clear()
    LogFileManager.init(getTargetContext())
    LoggerFactory.clearLogAdapters()
    LoggerFactory.addLogAdapter(object : LogAdapter {
      override fun isLoggable(logLine: LogLine): Boolean {
        return true
      }

      override fun log(logLine: LogLine) {
        logs.add(logLine)
      }
    })
    logger = LoggerFactory.create("")
  }

  @After
  fun after() {
    logs.clear()
  }


  @Test
  fun doNotLogIfNotLoggable() {
    logger.clearLogAdapters()
    logger.d("doNotLogIfNotLoggable")
    Assert.assertEquals(0, logs.size)
    logger.addLogAdapter(object : LogAdapter {
      override fun isLoggable(logLine: LogLine): Boolean {
        return logLine.level >= INFO
      }

      override fun log(logLine: LogLine) {
        logs.add(logLine)
      }
    })
    //isLoggable = logLine.level >= INFO
    logger.d("doNotLogIfNotLoggable")
    Assert.assertEquals(0, logs.size)

    logger.e("doNotLogIfNotLoggable")
    Assert.assertEquals(1, logs.size)
  }

  @Test
  fun v() {
    logger.v("log i %s", "VERBOSE")
    verifyLevel(VERBOSE)
    verifyTime()
    verifyTag("")
    verifyMessage("log i VERBOSE")
  }


  @Test
  fun i() {
    logger.i("log i", Throwable("Throwable"))
    verifyCallCount(1)
    verifyLevel(INFO)
    verifyTime()
    verifyTag("")
    verify {
      Assert.assertTrue(it.message.contains("log i"))
      Assert.assertFalse(it.message.contains("Throwable"))
    }
  }

  @Test
  fun wtf() {
    logger.wtf("log %s", "wtf")
    verifyCallCount(1)
    verifyLevel(ASSERT)
    verifyTime()
    verifyTag("")
    verifyMessage("log wtf")
  }


  @Test
  fun log() {
    logger.log(LogLine.obtain(DEBUG, null, "logger.log"))
    verifyCallCount(1)
    verifyLevel(DEBUG)
    verifyTime()
    verifyTag("")
    verifyMessage("logger.log")
  }

  @Test
  fun w() {
    logger.w("log %s", "warn")
    verifyCallCount(1)
    verifyLevel(WARN)
    verifyTime()
    verifyTag("")
    verifyMessage("log warn")
  }

  @Test
  fun d() {
    logger.d("log %s", "Logger.d")
    verifyCallCount(1)
    verifyLevel(DEBUG)
    verifyTime()
    verifyTag("")
    verifyMessage("log Logger.d")
  }

  @Test
  fun e() {
    logger.e(Throwable("static error log Throwable"), "log %s", "Logger.e(ERROR)")
    verifyCallCount(1)
    verifyLevel(ERROR)
    verifyTime()
    verifyTag("")
    verify {
      Assert.assertTrue(it.message.contains("log Logger.e(ERROR)"))
      Assert.assertTrue(it.message.contains("static error log Throwable"))
    }
  }

  @Test
  fun json() {
    // array
    logger.json("  {\"key\":3}")
    verifyCallCount(1)
    verifyLevel(DEBUG)
    verifyTime()
    verifyMessage(JSONObject("{\"key\":3}").toString(LogLine.JSON_INDENT))

    // object
    logger.json("  [{\"key\":3}]")
    verifyCallCount(2)
    verifyTime()
    verify(1) { log ->
      Assert.assertEquals(JSONArray("[{\"key\":3}]").toString(LogLine.JSON_INDENT), log.message)
    }
  }

  @Test
  fun invalidJson() {
    //Invalid
    logger.json("no json")
    logger.json("{ missing end")
    logger.json(null)
    logger.json("")
    verifyCallCount(4)
    verifyLevel(DEBUG)
    verify(0, 1) {
      Assert.assertEquals("Invalid Json", it.message)
    }
    verify(2, 3) {
      Assert.assertEquals("Empty/Null json content", it.message)
    }

  }

  @Test
  fun xml() {
    val xml = "<xml>Test</xml>"
    logger.xml(xml)
    verifyCallCount(1)
    verifyLevel(DEBUG)
    verify {
      Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml>Test</xml>\n", it.message)
    }
  }

  @Test
  fun invalidXml() {
    val xml = "xml>Test</xml>"
    logger.xml(xml)
    logger.xml(null)
    logger.xml("")
    verifyCallCount(3)
    verifyLevel(DEBUG)
    verify {
      Assert.assertEquals("Invalid xml", it.message)
    }
    verify(1, 2) {
      Assert.assertEquals("Empty/Null xml content", it.message)
    }
  }

  /**
   * @see com.erlei.logger.AlignArgsFormatterTest
   * @see com.erlei.logger.SimpleArgsFormatterTest
   */
  @Test
  fun args() {
    logger.args(1, 6, 7, 30, 33)
    verifyCallCount(1)
    verifyLevel(DEBUG)
  }

  private fun verify(vararg index: Int = intArrayOf(0), function: (LogLine) -> Unit) {
    index.forEach {
      function.invoke(logs[it])
    }
  }


  private fun verifyLevel(@Level level: Int) {
    verify {
      Assert.assertEquals(level, it.level)
    }
  }

  private fun verifyTag(tag: String) {
    verify { Assert.assertEquals(tag, it.fulTag) }
  }

  private fun verifyTime() {
    verify { Assert.assertEquals(System.currentTimeMillis() / 500, it.time / 500) }
  }


  private fun verifyMessage(msg: String) {
    verify { Assert.assertEquals(msg, it.message) }
  }

  private fun verifyCallCount(time: Int) {
    Assert.assertEquals(time, logs.size)
  }
}