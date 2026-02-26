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
import com.temporal.app.ui.components.TemporalTopBar

@Composable
fun AnalysisScreen(nav: NavController) {
  val ctx = LocalContext.current
  var insights by remember { mutableStateOf(emptyList<com.temporal.app.analysis.Insight>()) }

  LaunchedEffect(Unit) {
    insights = AnalysisEngine.weeklyInsights(ctx, System.currentTimeMillis())
  }

  Scaffold(topBar = { TemporalTopBar("Analiz", onBack = { nav.popBackStack() }) }) { pad ->
    LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      item {
        Card { Column(Modifier.padding(16.dp)) {
          Text("Otomatik Çıktılar", style = MaterialTheme.typography.titleMedium)
          Spacer(Modifier.height(6.dp))
          if (insights.isEmpty()) Text("Henüz yeterli veri yok. Log girdikçe analiz güçlenecek.")
          insights.forEach {
            Text("• ${it.title}: ${it.detail}")
          }
        } }
      }
      item {
        Card { Column(Modifier.padding(16.dp)) {
          Text("Hedef", style = MaterialTheme.typography.titleMedium)
          Spacer(Modifier.height(6.dp))
          Text("Nöbet olan günler vs olmayan günler karşılaştırması, ortak kalıplar ve risk uyarıları bu ekranda büyüyecek.")
        } }
      }
    }
  }
}
