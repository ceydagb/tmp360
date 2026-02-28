package com.temporal.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SmokeReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val title = intent.getStringExtra("title") ?: "Sigara plani"
    val text = intent.getStringExtra("text") ?: "Planlanan sigara zamani geldi."
    val route = intent.getStringExtra("route") ?: "smoke_plan"
    Notifier.show(context, title, text, route)
  }
}
