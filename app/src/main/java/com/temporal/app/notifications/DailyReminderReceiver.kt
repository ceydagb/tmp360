package com.temporal.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    // Bu sürümde bildirimleri WorkManager (LifeNudgeWorker) üretiyor.
    // Receiver'ı boş bırakıyoruz.
  }
}
