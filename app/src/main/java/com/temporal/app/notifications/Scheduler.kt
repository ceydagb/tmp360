package com.temporal.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object Scheduler {

  fun ensureDailyReminders(context: Context) {
    val req = PeriodicWorkRequestBuilder<LifeNudgeWorker>(15, TimeUnit.MINUTES).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      "temporal_nudges",
      ExistingPeriodicWorkPolicy.UPDATE,
      req
    )
  }
}
