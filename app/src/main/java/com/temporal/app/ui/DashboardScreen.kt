package com.temporal.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.analysis.AnalysisEngine
import com.temporal.app.analysis.Insight
import com.temporal.app.data.AppSettings
import com.temporal.app.data.SettingsStore
import com.temporal.app.data.TemporalDb
import com.temporal.app.ui.components.ChartItem
import com.temporal.app.ui.components.SimpleBarChart
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.util.Day
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val settingsStore = remember { SettingsStore(ctx) }
  val settings by settingsStore.flow.collectAsState(initial = AppSettings())

  val now = remember { System.currentTimeMillis() }
  val dayStart = remember(now) { Day.startOfDay(now) }
  val dayEnd = remember(dayStart) { Day.endOfDay(dayStart) }
  val weekStart = remember(dayStart) { dayStart - TimeUnit.DAYS.toMillis(6) }

  val watersToday by dao.waterBetween(dayStart, dayEnd).collectAsState(initial = emptyList())
  val mealsToday by dao.mealsBetween(dayStart, dayEnd).collectAsState(initial = emptyList())
  val sleepsWeek by dao.sleepBetween(weekStart, dayEnd).collectAsState(initial = emptyList())
  val seizuresWeek by dao.seizureBetween(weekStart, dayEnd).collectAsState(initial = emptyList())

  var risk by remember { mutableFloatStateOf(0f) }
  var insights by remember { mutableStateOf<List<Insight>>(emptyList()) }
  var weeklyWaterAvg by remember { mutableFloatStateOf(0f) }
  var weeklyCarbAvg by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(watersToday.size, mealsToday.size, sleepsWeek.size, seizuresWeek.size) {
    val ts = System.currentTimeMillis()
    risk = AnalysisEngine.dailyRisk0to10(ctx, ts)
    insights = AnalysisEngine.weeklyInsights(ctx, ts)

    val (waterAvg, carbAvg) = withContext(Dispatchers.IO) {
      val water = dao.waterSum(ts - TimeUnit.DAYS.toMillis(7), ts) / 7f
      val carb = dao.carbSum(ts - TimeUnit.DAYS.toMillis(7), ts) / 7f
      water to carb
    }
    weeklyWaterAvg = waterAvg
    weeklyCarbAvg = carbAvg
  }

  val waterMl = remember(watersToday) { watersToday.sumOf { it.amountMl } }
  val carbsG = remember(mealsToday) { mealsToday.sumOf { it.carbsG } }
  val sleepHours = remember(sleepsWeek) {
    val yStart = Day.startOfDay(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))
    val yesterdaySleep = sleepsWeek.firstOrNull { it.dateStart == yStart }
    if (yesterdaySleep == null) 0f else ((yesterdaySleep.wake - yesterdaySleep.sleepStart) / 3600000f)
  }

  Scaffold(
    topBar = { TemporalTopBar("Temporal") },
    floatingActionButton = {
      FloatingActionButton(onClick = { nav.navigate(Routes.AddMeal) }) {
        Icon(Icons.Default.Add, contentDescription = "Ekle")
      }
    }
  ) { pad ->
    Column(
      modifier = Modifier
        .padding(pad)
        .padding(16.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Card {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Gunluk Risk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${"%.1f".format(risk)} / 10", style = MaterialTheme.typography.headlineSmall)
            Text(
              if (risk >= 7f) "Yuksek risk: kayitlari tamamlayin"
              else if (risk >= 4f) "Orta risk: su, uyku ve ogunleri takip edin"
              else "Dusuk risk",
              style = MaterialTheme.typography.bodySmall
            )
          }
          CircularProgressIndicator(
            progress = { (risk / 10f).coerceIn(0f, 1f) },
            modifier = Modifier.size(64.dp),
            strokeWidth = 7.dp
          )
        }
      }

      Text("Hizli giris", style = MaterialTheme.typography.titleMedium)

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { nav.navigate(Routes.AddWater) }) { Text("Su ekle") }
        Button(onClick = { nav.navigate(Routes.AddMeal) }) { Text("Ogun ekle") }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { nav.navigate(Routes.AddSeizure) }) { Text("Nobet") }
        Button(onClick = { nav.navigate(Routes.AddAura) }) { Text("Aura") }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { nav.navigate(Routes.AddActivity) }) { Text("Aktivite") }
        Button(onClick = { nav.navigate(Routes.AddMood) }) { Text("Duygu") }
      }

      HorizontalDivider()

      Card {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("Bugun", style = MaterialTheme.typography.titleMedium)
          Text("Su: $waterMl ml / ${settings.waterTargetMl} ml")
          Text("Karbonhidrat: $carbsG g / ${settings.dailyCarbTarget} g")
          Text("Dunku uyku: ${"%.1f".format(sleepHours)} saat")
          Text("Son 7 gunde nobet: ${seizuresWeek.size}")
        }
      }

      SimpleBarChart(
        title = "Haftalik ozet",
        items = listOf(
          ChartItem("Su hedefi", weeklyWaterAvg / settings.waterTargetMl.coerceAtLeast(1)),
          ChartItem("Karb limiti", weeklyCarbAvg / settings.dailyCarbTarget.coerceAtLeast(1)),
          ChartItem("Uyku hedefi", sleepHours / 8f)
        )
      )

      if (insights.isNotEmpty()) {
        Card {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("Haftalik icgoruler", style = MaterialTheme.typography.titleMedium)
            insights.take(3).forEach { insight ->
              Text("- ${insight.title}: ${insight.detail}", style = MaterialTheme.typography.bodySmall)
            }
          }
        }
      }
    }
  }
}
