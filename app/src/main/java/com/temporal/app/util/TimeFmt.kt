package com.temporal.app.util
import java.text.SimpleDateFormat
import java.util.*
object TimeFmt {
  private val dt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("tr","TR"))
  private val d = SimpleDateFormat("dd MMM yyyy", Locale("tr","TR"))
  fun format(ts: Long) = dt.format(Date(ts))
  fun formatDate(ts: Long) = d.format(Date(ts))
}
