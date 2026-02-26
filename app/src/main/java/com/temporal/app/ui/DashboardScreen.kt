package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.analysis.AnalysisEngine
import com.temporal.app.data.SettingsStore
import com.temporal.app.data.TemporalDb
import com.temporal.app.ui.components.RiskRing
import com.temporal.app.ui.components.SimpleBarChart
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.util.Day
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(nav: NavController) {
  val ctx = LocalContext.current
  val scope = rememberCoroutineScope()
  var risk by remember { mutableStateOf(0f) }
  var riskSubtitle by remember { mutableStateOf("Hesaplanıyor…") }
  var waterToday by remember { mutableStateOf(0) }
  var kcalToday by remember { mutableStateOf(0) }
  var carbToday by remember { mutableStateOf(0) }
  var seizures7 by remember { mutableStateOf(0) }
  var weeklyRiskBars by remember { mutableStateOf(listOf(0,0,0,0,0,0,0)) }

  LaunchedEffect(Unit) {
    val now = System.currentTimeMillis()
    risk = AnalysisEngine.dailyRisk0to10(ctx, now)
    riskSubtitle = when {
      risk >= 8 -> "Yüksek"
      risk >= 5 -> "Orta"
      else -> "Düşük"
    }
    val dao = TemporalDb.get(ctx).dao()
    val todayFrom = Day.startOfDay(now)
    val todayTo = Day.endOfDay(now)
    waterToday = dao.waterSum(todayFrom, todayTo)
    kcalToday = dao.kcalSum(todayFrom, todayTo)
    carbToday = dao.carbSum(todayFrom, todayTo)
    seizures7 = dao.seizureCount(now - TimeUnit.DAYS.toMillis(7), now)

    // weekly risk bars: naive (use daily risk calc)
    val bars = mutableListOf<Int>()
    for (i in 6 downTo 0) {
      val t = now - TimeUnit.DAYS.toMillis(i.toLong())
      bars += AnalysisEngine.dailyRisk0to10(ctx, t).toInt()
    }
    weeklyRiskBars = bars
  }

  Scaffold(
    topBar = {
      TemporalTopBar("Temporal", actions = {
        IconButton(onClick = { nav.navigate(Routes.Export) }) { Icon(Icons.Default.Share, "Dışa Aktar") }
      })
    }
  ) { pad ->
    LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

      item {
        Card {
          Column(Modifier.padding(16.dp)) {
            RiskRing(risk, "Bugün: $riskSubtitle")
            Spacer(Modifier.height(12.dp))
            Text("Gün Özeti", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("Su: $waterToday ml • Kalori: $kcalToday kcal • Karb: $carbToday g • 7g nöbet: $seizures7")
          }
        }
      }

      item {
        Card { Column(Modifier.padding(16.dp)) {
          SimpleBarChart("Haftalık Risk (0–10)", weeklyRiskBars, listOf("P","S","Ç","P","C","C","P"))
        } }
      }

      item {
        Text("Hızlı Giriş", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          FilledTonalButton(onClick = { nav.navigate(Routes.AddWater) }, modifier = Modifier.weight(1f)) { Text("Su") }
          FilledTonalButton(onClick = { nav.navigate(Routes.AddMeal) }, modifier = Modifier.weight(1f)) { Text("Öğün") }
          FilledTonalButton(onClick = { nav.navigate(Routes.AddSeizure) }, modifier = Modifier.weight(1f)) { Text("Nöbet") }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          FilledTonalButton(onClick = { nav.navigate(Routes.AddAura) }, modifier = Modifier.weight(1f)) { Text("Aura") }
          FilledTonalButton(onClick = { nav.navigate(Routes.SmokePlan) }, modifier = Modifier.weight(1f)) { Text("Sigara Plan") }
          FilledTonalButton(onClick = { nav.navigate(Routes.AddSleep) }, modifier = Modifier.weight(1f)) { Text("Uyku") }
        }
      }
    }
  }
}
