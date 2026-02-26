package com.temporal.app.ui.components
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.util.Calendar

fun showDatePicker(context: Context, initial: Long, onPicked: (Long) -> Unit) {
  val cal = Calendar.getInstance().apply { timeInMillis = initial }
  DatePickerDialog(context, { _, y, m, d ->
    val out = Calendar.getInstance().apply {
      timeInMillis = initial
      set(Calendar.YEAR, y); set(Calendar.MONTH, m); set(Calendar.DAY_OF_MONTH, d)
    }
    onPicked(out.timeInMillis)
  }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
}

fun showTimePicker(context: Context, initial: Long, onPicked: (Long) -> Unit) {
  val cal = Calendar.getInstance().apply { timeInMillis = initial }
  TimePickerDialog(context, { _, hh, mm ->
    val out = Calendar.getInstance().apply {
      timeInMillis = initial
      set(Calendar.HOUR_OF_DAY, hh); set(Calendar.MINUTE, mm); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
    }
    onPicked(out.timeInMillis)
  }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
}
