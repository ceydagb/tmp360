package com.temporal.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SmokeReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    // Bu sürümde sigara hatırlatmaları WorkManager + plan modülü ile yürür.
  }
}
