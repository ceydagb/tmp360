package com.temporal.app.analysis

import android.content.Context
import com.temporal.app.data.TemporalDb
import com.temporal.app.util.Day
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class Insight(val title:String, val detail:String)
data class RiskBreakdown(val score: Float, val reasons: List<String>)
data class SeizureComparisonRow(
  val metric: String,
  val seizureAvg: Float,
  val nonSeizureAvg: Float,
  val unit: String
)
data class TriggerComboStat(
  val combo: String,
  val seizureDays: Int,
  val nonSeizureDays: Int,
  val seizureRate: Float
)
data class Phase2Report(
  val lookbackDays: Int,
  val seizureDays: Int,
  val nonSeizureDays: Int,
  val comparisonRows: List<SeizureComparisonRow>,
  val topCombos: List<TriggerComboStat>
)

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

  suspend fun phase2Report(context: Context, now: Long, lookbackDays: Int = 30): Phase2Report =
    withContext(Dispatchers.IO) {
      val dao = TemporalDb.get(context).dao()
      val dayMs = TimeUnit.DAYS.toMillis(1)
      val baseDay = Day.startOfDay(now)

      data class DayFeatures(
        val seizure: Boolean,
        val waterMl: Int,
        val carbsG: Int,
        val sleepHours: Float,
        val auraCount: Int,
        val activityMin: Int,
        val negativeMoodCount: Int,
        val activeTriggers: Set<String>
      )

      val days = mutableListOf<DayFeatures>()

      for (i in 0 until lookbackDays) {
        val dayStart = baseDay - i * dayMs
        val dayEnd = dayStart + dayMs

        val seizure = dao.seizureCount(dayStart, dayEnd) > 0
        val water = dao.waterSum(dayStart, dayEnd)
        val carbs = dao.carbSum(dayStart, dayEnd)
        val aura = dao.auraCount(dayStart, dayEnd)
        val activity = dao.activityDurationSum(dayStart, dayEnd)
        val negMood = dao.negativeMoodCount(dayStart, dayEnd)
        val sleep = dao.sleepByDate(dayStart)
        val sleepH = if (sleep == null) 0f else ((sleep.wake - sleep.sleepStart) / 3600000f)

        val triggers = mutableSetOf<String>()
        if (water in 1..1499) triggers += "low_water"
        if (carbs > 50) triggers += "high_carb"
        if (sleepH in 0.01f..5.99f || sleep == null) triggers += "short_sleep"
        if (aura > 0) triggers += "aura_present"
        if (negMood > 0) triggers += "negative_mood"
        if (activity in 0..19) triggers += "low_activity"

        days += DayFeatures(
          seizure = seizure,
          waterMl = water,
          carbsG = carbs,
          sleepHours = sleepH,
          auraCount = aura,
          activityMin = activity,
          negativeMoodCount = negMood,
          activeTriggers = triggers
        )
      }

      val seizureDays = days.filter { it.seizure }
      val nonSeizureDays = days.filter { !it.seizure }

      fun avg(src: List<DayFeatures>, metric: (DayFeatures) -> Number): Float {
        if (src.isEmpty()) return 0f
        return (src.sumOf { metric(it).toDouble() } / src.size).toFloat()
      }

      val rows = listOf(
        SeizureComparisonRow("Su", avg(seizureDays) { it.waterMl }, avg(nonSeizureDays) { it.waterMl }, "ml"),
        SeizureComparisonRow("Karbonhidrat", avg(seizureDays) { it.carbsG }, avg(nonSeizureDays) { it.carbsG }, "g"),
        SeizureComparisonRow("Uyku", avg(seizureDays) { it.sleepHours }, avg(nonSeizureDays) { it.sleepHours }, "saat"),
        SeizureComparisonRow("Aura", avg(seizureDays) { it.auraCount }, avg(nonSeizureDays) { it.auraCount }, "adet"),
        SeizureComparisonRow("Aktivite", avg(seizureDays) { it.activityMin }, avg(nonSeizureDays) { it.activityMin }, "dk"),
        SeizureComparisonRow("Negatif duygu", avg(seizureDays) { it.negativeMoodCount }, avg(nonSeizureDays) { it.negativeMoodCount }, "adet")
      )

      data class ComboAccumulator(var seiz: Int = 0, var non: Int = 0)
      val comboMap = linkedMapOf<String, ComboAccumulator>()

      fun normalizeCombo(a: String, b: String): String {
        return listOf(a, b).sorted().joinToString(" + ")
      }

      days.forEach { d ->
        val tr = d.activeTriggers.toList().sorted()
        for (x in 0 until tr.size) {
          for (y in x + 1 until tr.size) {
            val key = normalizeCombo(tr[x], tr[y])
            val acc = comboMap.getOrPut(key) { ComboAccumulator() }
            if (d.seizure) acc.seiz++ else acc.non++
          }
        }
      }

      fun displayName(raw: String): String = when (raw) {
        "low_water" -> "Dusuk su"
        "high_carb" -> "Yuksek karbonhidrat"
        "short_sleep" -> "Kisa uyku"
        "aura_present" -> "Aura var"
        "negative_mood" -> "Negatif duygu"
        "low_activity" -> "Dusuk aktivite"
        else -> raw
      }

      val topCombos = comboMap
        .mapNotNull { (combo, acc) ->
          val total = acc.seiz + acc.non
          if (total < 2) return@mapNotNull null
          val rate = if (total == 0) 0f else (acc.seiz.toFloat() / total.toFloat())
          TriggerComboStat(
            combo = combo.split(" + ").joinToString(" + ") { displayName(it) },
            seizureDays = acc.seiz,
            nonSeizureDays = acc.non,
            seizureRate = rate
          )
        }
        .sortedWith(compareByDescending<TriggerComboStat> { it.seizureRate }.thenByDescending { it.seizureDays })
        .take(3)

      Phase2Report(
        lookbackDays = lookbackDays,
        seizureDays = seizureDays.size,
        nonSeizureDays = nonSeizureDays.size,
        comparisonRows = rows,
        topCombos = topCombos
      )
    }
}
