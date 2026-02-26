package com.temporal.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar
import java.util.concurrent.TimeUnit

object Scheduler {

  fun ensureAll(context: Context) {
    NotificationChannels.ensure(context)
    scheduleDaily(context, 10, 0, "aura_10", RoutesTarget.AURA)
    scheduleDaily(context, 13, 0, "meal_13", RoutesTarget.MEAL)
    scheduleDaily(context, 16, 0, "aura_16", RoutesTarget.AURA)
    scheduleDaily(context, 20, 0, "meal_20", RoutesTarget.MEAL)
    scheduleDaily(context, 11, 0, "sleep_11", RoutesTarget.SLEEP)

    scheduleInterval(context, 4, "place_4h")
  }

  private fun scheduleDaily(context: Context, hour:Int, minute:Int, reqTag:String, target: RoutesTarget) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val i = Intent(context, DailyReminderReceiver::class.java).apply {
      putExtra("target", target.name)
      putExtra("tag", reqTag)
    }
    val pi = PendingIntent.getBroadcast(context, reqTag.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val cal = Calendar.getInstance().apply {
      set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
      set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute)
      if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
    }
    am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, TimeUnit.DAYS.toMillis(1), pi)
  }

  enum class RoutesTarget { AURA, MEAL, SLEEP }
}
