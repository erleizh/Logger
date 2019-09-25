package com.erlei.logger

import android.util.Log
import com.erlei.logger.format.SimpleArgsFormatter
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class SimpleArgsFormatterTest {

  private lateinit var formatter: SimpleArgsFormatter

  @Before
  fun before() {
    formatter = SimpleArgsFormatter("\t")
  }

  @Test
  fun testVararg() {
    Assert.assertEquals(
        "${Int.MIN_VALUE}\t${Float.MIN_VALUE}\t${Long.MAX_VALUE}\t${Double.MIN_VALUE}\t${Math.PI}",
        formatter.format(Int.MIN_VALUE, Float.MIN_VALUE, Long.MAX_VALUE, Double.MIN_VALUE, Math.PI))
  }

  @Test
  fun logObject() {
    val obj = Size()
    Assert.assertEquals(obj.toString(), formatter.format(obj))
  }

  @Test
  fun logArray() {
    val obj = intArrayOf(1, 6, 7, 30, 33)
    Assert.assertEquals(Arrays.toString(obj), formatter.format(obj))
  }

  @Test
  fun logStringArray() {
    val obj = arrayOf("a", "b", "c")
    Assert.assertEquals(Arrays.toString(obj), formatter.format(obj))
  }

  @Test
  fun logMultiDimensionArray() {
    val doubles = arrayOf(doubleArrayOf(1.0, 6.0), doubleArrayOf(1.2, 33.0))
    Assert.assertEquals("[[1.0, 6.0], [1.2, 33.0]]", formatter.format(doubles))
  }

  @Test
  fun logList() {
    val list = Arrays.asList("foo", "bar", null)
    Assert.assertEquals(list.toString(), formatter.format(list))
  }

  @Test
  fun logMap() {
    val map = HashMap<String, String>()
    map["key"] = "value"
    map["key2"] = "value2"
    Assert.assertEquals(map.toString(), formatter.format(map))
  }

  @Test
  fun logSet() {
    val set = HashSet<String>()
    set.add("key")
    set.add("key1")

    Assert.assertEquals(set.toString(), formatter.format(set))
  }

  @Test
  fun logThrowable() {
    val throwable = Throwable("test logThrowable")
    Assert.assertEquals(Log.getStackTraceString(throwable), formatter.format(throwable))
  }

}

data class Size(val width: Int = 0, val height: Int = 0)