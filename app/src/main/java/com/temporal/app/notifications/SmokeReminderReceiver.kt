package com.temporal.app.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.temporal.app.MainActivity
import com.temporal.app.ui.Routes

class SmokeReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    NotificationChannels.ensure(context)
    val open = Intent(context, MainActivity::class.java).apply {
      putExtra("route", Routes.SmokePlan)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pi = PendingIntent.getActivity(context, 991, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val n = NotificationCompat.Builder(context, NotificationChannels.SMOKE)
      .setSmallIcon(android.R.drawable.ic_dialog_alert)
      .setContentTitle("Sigara zamanı (plan)")
      .setContentText("Planına göre zaman geldi. İstersen planı güncelle.")
      .setAutoCancel(true)
      .setContentIntent(pi)
      .build()
    NotificationManagerCompat.from(context).notify(991, n)
  }
}
