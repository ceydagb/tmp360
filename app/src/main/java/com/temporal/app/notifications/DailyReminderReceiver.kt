package com.temporal.app.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.temporal.app.MainActivity
import com.temporal.app.R
import com.temporal.app.ui.Routes

class DailyReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    NotificationChannels.ensure(context)

    val target = intent.getStringExtra("target") ?: Scheduler.RoutesTarget.AURA.name
    val route = when (Scheduler.RoutesTarget.valueOf(target)) {
      Scheduler.RoutesTarget.AURA -> Routes.AddAura
      Scheduler.RoutesTarget.MEAL -> Routes.AddMeal
      Scheduler.RoutesTarget.SLEEP -> Routes.AddSleep
    }

    val open = Intent(context, MainActivity::class.java).apply {
      putExtra("route", route)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pi = PendingIntent.getActivity(context, route.hashCode(), open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val title = when (route) {
      Routes.AddAura -> "Aura belirtisi var mı?"
      Routes.AddMeal -> "Öğün kontrolü"
      else -> "Uyku takibi"
    }
    val text = when (route) {
      Routes.AddAura -> "Varsa türünü seçip kaydet."
      Routes.AddMeal -> "Öğün ekle / atlandı ise not düş."
      else -> "Dün kaçta uyudun/uyandın?"
    }

    val n = NotificationCompat.Builder(context, NotificationChannels.DAILY)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(title)
      .setContentText(text)
      .setAutoCancel(true)
      .setContentIntent(pi)
      .build()

    NotificationManagerCompat.from(context).notify((route.hashCode() and 0x7fffffff), n)
  }
}
