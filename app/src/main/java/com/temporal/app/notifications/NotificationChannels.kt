package com.temporal.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
  const val GENERAL = "temporal_general"

  fun ensure(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val ch = NotificationChannel(
      GENERAL,
      "Temporal Bildirimleri",
      NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
      description = "Hatırlatmalar ve uyarılar"
    }

    mgr.createNotificationChannel(ch)
  }
}
