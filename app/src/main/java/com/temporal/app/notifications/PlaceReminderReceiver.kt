package com.temporal.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlaceReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    // Bu sürümde yer bildirimleri WorkManager üzerinden yürür.
  }
}
