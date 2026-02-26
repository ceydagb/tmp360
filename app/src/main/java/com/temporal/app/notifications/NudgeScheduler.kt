package com.temporal.app.notifications

import android.content.Context

object NudgeScheduler {
  fun ensureScheduled(context: Context) {
    // Şimdilik Scheduler ile aynı Worker mantığı kullanıyoruz.
    Scheduler.ensureDailyReminders(context)
  }
}
