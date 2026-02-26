package com.temporal.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
  const val DAILY = "daily"
  const val SMOKE = "smoke"
  const val RISK = "risk"

  fun ensure(context: Context) {
    if (Build.VERSION.SDK_INT < 26) return
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    fun ch(id:String, name:String) {
      nm.createNotificationChannel(NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT))
    }
    ch(DAILY, "Günlük hatırlatmalar")
    ch(SMOKE, "Sigara planı")
    ch(RISK, "Risk uyarıları")
  }
}
