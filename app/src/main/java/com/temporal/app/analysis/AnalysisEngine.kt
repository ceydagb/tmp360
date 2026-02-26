package com.temporal.app.analysis

import android.content.Context
import com.temporal.app.data.TemporalDb
import com.temporal.app.util.Day
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class Insight(val title:String, val detail:String)

object AnalysisEngine {

  suspend fun dailyRisk0to10(context: Context, now: Long): Float = withContext(Dispatchers.IO) {
    val dao = TemporalDb.get(context).dao()
    val from = now - TimeUnit.HOURS.toMillis(24)
    var score = 0f

    val waterMl = dao.waterSum(Day.startOfDay(now), Day.endOfDay(now))
    if (waterMl < 1500) score += 2.0f
    if (waterMl < 1000) score += 1.5f

    val seizures24 = dao.seizureCount(from, now)
    if (seizures24 > 0) score = 10f

    val carbs = dao.carbSum(Day.startOfDay(now), Day.endOfDay(now))
    if (carbs > 50) score += 1.5f
    if (carbs > 80) score += 1.5f

    // sleep (if missing yesterday) mild risk
    val yStart = Day.startOfDay(now - TimeUnit.DAYS.toMillis(1))
    val sleep = dao.sleepByDate(yStart)
    if (sleep == null) score += 1.0f else {
      val durH = (sleep.wake - sleep.sleepStart) / 3600000.0
      if (durH < 6) score += 2.0f
      if (durH < 5) score += 1.5f
    }

    score.coerceIn(0f,10f)
  }

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
