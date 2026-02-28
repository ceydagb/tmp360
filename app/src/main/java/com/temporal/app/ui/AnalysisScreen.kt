package com.temporal.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.temporal.app.analysis.Phase2Report
import com.temporal.app.ui.components.ChartItem
import com.temporal.app.ui.components.SimpleBarChart
import com.temporal.app.ui.components.TemporalTopBar

@Composable
fun AnalysisScreen(nav: NavController) {
  val ctx = LocalContext.current
  var insights by remember { mutableStateOf(emptyList<Insight>()) }
  var phase2 by remember { mutableStateOf<Phase2Report?>(null) }

  LaunchedEffect(Unit) {
    val ts = System.currentTimeMillis()
    insights = AnalysisEngine.weeklyInsights(ctx, ts)
    phase2 = AnalysisEngine.phase2Report(ctx, ts, lookbackDays = 30)
  }

  Scaffold(topBar = { TemporalTopBar("Analiz", onBack = { nav.popBackStack() }) }) { pad ->
    LazyColumn(
      modifier = Modifier
        .padding(pad)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Card {
          Column(Modifier.padding(16.dp)) {
            Text("Otomatik Ciktilar", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            if (insights.isEmpty()) Text("Henuz yeterli veri yok. Log girdikce analiz guclenecek.")
            insights.forEach {
              Text("- ${it.title}: ${it.detail}")
            }
          }
        }
      }

      val p2 = phase2
      if (p2 != null) {
        item {
          Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("Faz-2: Nobetli vs Nobetsiz Gunler", style = MaterialTheme.typography.titleMedium)
              Text(
                "Son ${p2.lookbackDays} gun: nobetli=${p2.seizureDays}, nobetsiz=${p2.nonSeizureDays}",
                style = MaterialTheme.typography.bodySmall
              )

              p2.comparisonRows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(row.metric, fontWeight = FontWeight.SemiBold)
                  Text(
                    "Nobetli ${"%.1f".format(row.seizureAvg)} ${row.unit} | Nobetsiz ${"%.1f".format(row.nonSeizureAvg)} ${row.unit}",
                    style = MaterialTheme.typography.bodySmall
                  )
                }
              }
            }
          }
        }

        item {
          val maxSeizureRate = (p2.topCombos.maxOfOrNull { it.seizureRate } ?: 1f).coerceAtLeast(0.01f)
          SimpleBarChart(
            title = "Top 3 Tetikleyici Kombinasyon",
            items = p2.topCombos.map {
              ChartItem(
                label = it.combo,
                value = (it.seizureRate / maxSeizureRate).coerceIn(0f, 1f)
              )
            }
          )
        }

        item {
          Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("Kombinasyon Detayi", style = MaterialTheme.typography.titleMedium)
              if (p2.topCombos.isEmpty()) {
                Text("Yeterli kombinasyon verisi yok.")
              } else {
                p2.topCombos.forEachIndexed { idx, c ->
                  Text(
                    "${idx + 1}. ${c.combo} -> nobetli gun ${c.seizureDays}, nobetsiz gun ${c.nonSeizureDays}, oran ${"%.0f".format(c.seizureRate * 100)}%"
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
