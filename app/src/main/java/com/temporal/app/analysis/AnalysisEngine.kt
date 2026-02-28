package com.temporal.app.analysis

import android.content.Context
import com.temporal.app.data.TemporalDb
import com.temporal.app.util.Day
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class Insight(val title:String, val detail:String)
data class RiskBreakdown(val score: Float, val reasons: List<String>)

object AnalysisEngine {

  suspend fun riskBreakdown(context: Context, now: Long): RiskBreakdown = withContext(Dispatchers.IO) {
    val dao = TemporalDb.get(context).dao()
    val from = now - TimeUnit.HOURS.toMillis(24)
    var score = 0f
    val reasons = mutableListOf<String>()

    val waterMl = dao.waterSum(Day.startOfDay(now), Day.endOfDay(now))
    if (waterMl < 1500) {
      score += 2.0f
      reasons += "Dusuk su alimi"
    }
    if (waterMl < 1000) {
      score += 1.5f
      reasons += "Ciddi dehidratasyon riski"
    }

    val seizures24 = dao.seizureCount(from, now)
    if (seizures24 > 0) {
      score = 10f
      reasons += "Son 24 saatte nobet kaydi var"
    }

    val carbs = dao.carbSum(Day.startOfDay(now), Day.endOfDay(now))
    if (carbs > 50) {
      score += 1.5f
      reasons += "Karbonhidrat yuksek"
    }
    if (carbs > 80) {
      score += 1.5f
      reasons += "Karbonhidrat hedefi ciddi asildi"
    }

    // sleep (if missing yesterday) mild risk
    val yStart = Day.startOfDay(now - TimeUnit.DAYS.toMillis(1))
    val sleep = dao.sleepByDate(yStart)
    if (sleep == null) {
      score += 1.0f
      reasons += "Uyku kaydi eksik"
    } else {
      val durH = (sleep.wake - sleep.sleepStart) / 3600000.0
      if (durH < 6) {
        score += 2.0f
        reasons += "Uyku suresi 6 saatin altinda"
      }
      if (durH < 5) {
        score += 1.5f
        reasons += "Uyku suresi cok dusuk"
      }
    }

    val finalScore = score.coerceIn(0f,10f)
    val finalReasons = reasons.distinct().take(3)
    RiskBreakdown(score = finalScore, reasons = finalReasons)
  }

  suspend fun dailyRisk0to10(context: Context, now: Long): Float =
    riskBreakdown(context, now).score

  suspend fun weeklyInsights(context: Context, now: Long): List<Insight> = withContext(Dispatchers.IO) {
    val dao = TemporalDb.get(context).dao()
    val weekFrom = now - TimeUnit.DAYS.toMillis(7)

    val seizures = dao.seizureCount(weekFrom, now)
    val waterAvg = dao.waterSum(weekFrom, now) / 7
    val carbsAvg = dao.carbSum(weekFrom, now) / 7

    val list = mutableListOf<Insight>()
    list += Insight("7 günde nöbet", "$seizures kayıt")
    list += Insight("Ortalama su", "${waterAvg} ml/gün")
    list += Insight("Ortalama karbonhidrat", "${carbsAvg} g/gün")

    val auraCounts = dao.auraCounts(weekFrom, now).take(3)
    if (auraCounts.isNotEmpty()) list += Insight("En sık aura", auraCounts.joinToString { "${it.auraType}(${it.c})" })

    val intake = dao.intakeTypeCounts(weekFrom, now)
    if (intake.isNotEmpty()) list += Insight("Takip edilen alımlar", intake.joinToString { "${it.type}:${it.c}" })

    list
  }
}
