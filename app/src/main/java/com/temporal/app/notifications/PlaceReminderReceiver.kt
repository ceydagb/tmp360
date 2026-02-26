package com.temporal.app.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.temporal.app.MainActivity
import com.temporal.app.ui.Routes

class PlaceReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    NotificationChannels.ensure(context)

    val open = Intent(context, MainActivity::class.java).apply {
      putExtra("route", Routes.AddPlace)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pi = PendingIntent.getActivity(context, Routes.AddPlace.hashCode(), open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val n = NotificationCompat.Builder(context, NotificationChannels.DAILY)
      .setSmallIcon(android.R.drawable.ic_menu_mylocation)
      .setContentTitle("Neredesin?")
      .setContentText("Konumu/yerini hızlıca kaydet.")
      .setAutoCancel(true)
      .setContentIntent(pi)
      .build()

    NotificationManagerCompat.from(context).notify(0x44AA11, n)
  }
}
