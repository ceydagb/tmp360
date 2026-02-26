package com.temporal.app.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.temporal.app.MainActivity
import com.temporal.app.R
import kotlin.random.Random

object Notifier {

  fun show(context: Context, title: String, text: String, route: String) {
    NotificationChannels.ensure(context)

    val i = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
      putExtra("route", route)
    }

    val pi = PendingIntent.getActivity(
      context,
      1000 + Random.nextInt(100000),
      i,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val n = NotificationCompat.Builder(context, NotificationChannels.GENERAL)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle(title)
      .setContentText(text)
      .setAutoCancel(true)
      .setContentIntent(pi)
      .build()

    NotificationManagerCompat.from(context).notify(2000 + Random.nextInt(100000), n)
  }
}
