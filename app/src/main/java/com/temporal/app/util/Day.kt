package com.temporal.app.util
import java.util.Calendar
import java.util.concurrent.TimeUnit
object Day {
  fun startOfDay(ts: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = ts
    c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0)
    return c.timeInMillis
  }
  fun endOfDay(ts: Long): Long = startOfDay(ts) + TimeUnit.DAYS.toMillis(1)
}
