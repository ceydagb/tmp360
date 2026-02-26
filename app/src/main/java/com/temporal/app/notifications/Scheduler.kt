package com.temporal.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object Scheduler {

  fun ensureDailyReminders(context: Context) {
    // Sabah/öğle/ikindi/akşam + uyku sorusu gibi bildirimleri
    // Worker içinde “şu an saat kaç” kontrol ederek atıyoruz.
    scheduleInterval(context, "temporal_daily_reminders", 15)
  }

  private fun scheduleInterval(context: Context, uniqueName: String, minutes: Long) {
    val req = PeriodicWorkRequestBuilder<LifeNudgeWorker>(minutes, TimeUnit.MINUTES)
      .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      uniqueName,
      ExistingPeriodicWorkPolicy.UPDATE,
      req
    )
  }
}
