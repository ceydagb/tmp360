package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.ui.components.ChartItem
import com.temporal.app.ui.components.SimpleBarChart
import com.temporal.app.ui.components.TemporalTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(nav: NavController) {
  Scaffold(
    topBar = { TemporalTopBar("Temporal") },
    floatingActionButton = {
      FloatingActionButton(onClick = { nav.navigate(Routes.AddMeal) }) {
        Icon(Icons.Default.Add, contentDescription = "Ekle")
      }
    }
  ) { pad ->
    Column(
      Modifier
        .padding(pad)
        .padding(16.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text("Hızlı giriş", style = MaterialTheme.typography.titleMedium)

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { nav.navigate(Routes.AddWater) }) { Text("Su ekle") }
        Button(onClick = { nav.navigate(Routes.AddMeal) }) { Text("Öğün ekle") }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { nav.navigate(Routes.AddSeizure) }) { Text("Nöbet") }
        Button(onClick = { nav.navigate(Routes.AddAura) }) { Text("Aura") }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { nav.navigate(Routes.AddActivity) }) { Text("Aktivite") }
        Button(onClick = { nav.navigate(Routes.AddMood) }) { Text("Duygu") }
      }

      Divider()

      // Şimdilik demo grafik (DB bağlayınca gerçek veri gelecek)
      SimpleBarChart(
        title = "Haftalık özet (demo)",
        items = listOf(
          ChartItem("Su hedefi", 0.55f),
          ChartItem("Karb hedefi", 0.35f),
          ChartItem("Uyku", 0.70f)
        )
      )
    }
  }
}
