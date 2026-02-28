package com.temporal.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MedicationReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val title = intent.getStringExtra("title") ?: "Ilac hatirlatici"
    val text = intent.getStringExtra("text") ?: "Planlanan ilac saatiniz geldi."
    val route = intent.getStringExtra("route") ?: "intake"
    Notifier.show(context, title, text, route)
  }
}
