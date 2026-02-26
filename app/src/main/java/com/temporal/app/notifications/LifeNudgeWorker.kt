package com.temporal.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar

class LifeNudgeWorker(
  private val ctx: Context,
  params: WorkerParameters
) : CoroutineWorker(ctx, params) {

  override suspend fun doWork(): Result {
    NotificationChannels.ensure(ctx)

    val now = Calendar.getInstance()
    val h = now.get(Calendar.HOUR_OF_DAY)
    val m = now.get(Calendar.MINUTE)

    // Dakikayı 0-2 aralığında yakalayıp 1 kez bildirim atıyoruz (Worker 15dk’da bir çalışıyor)
    val near = (m in 0..2)

    if (near) {
      when (h) {
        10 -> Notifier.show(ctx, "Aura belirtisi var mı?", "Varsa hızlıca kaydet", route = "add_aura")
        13 -> Notifier.show(ctx, "Öğle öğünü girdin mi?", "Girmediysen ekleyebilirsin", route = "add_meal")
        16 -> Notifier.show(ctx, "Ara kontrol", "Su / duygu / aktivite güncelle", route = "dashboard")
        20 -> Notifier.show(ctx, "Akşam kontrol", "Gün sonu kayıtlarını tamamla", route = "dashboard")
        11 -> Notifier.show(ctx, "Uyku takibi", "Dün kaçta uyudun/uyandın?", route = "add_sleep")
      }
    }

    // 4 saatte bir “Neredesin?” (08,12,16,20 gibi)
    if (near && h % 4 == 0) {
      Notifier.show(ctx, "Neredesin?", "Konumunu kaydet", route = "add_place")
    }

    return Result.success()
  }
}
