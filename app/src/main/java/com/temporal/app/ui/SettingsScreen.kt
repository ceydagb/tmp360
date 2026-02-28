package com.temporal.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.AppSettings
import com.temporal.app.data.SettingsStore
import com.temporal.app.ui.components.TemporalTopBar
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(nav: NavController) {
  val ctx = LocalContext.current
  val scope = rememberCoroutineScope()
  val store = remember { SettingsStore(ctx) }
  val s by store.flow.collectAsState(initial = AppSettings())

  var bottle by remember { mutableStateOf(s.bottleMl.toString()) }
  var target by remember { mutableStateOf(s.waterTargetMl.toString()) }
  var kcal by remember { mutableStateOf(s.dailyKcalTarget.toString()) }
  var carb by remember { mutableStateOf(s.dailyCarbTarget.toString()) }

  LaunchedEffect(s) {
    bottle = s.bottleMl.toString()
    target = s.waterTargetMl.toString()
    kcal = s.dailyKcalTarget.toString()
    carb = s.dailyCarbTarget.toString()
  }

  fun toggle(name: String, v: Boolean) { scope.launch { store.setModule(name, v) } }

  Scaffold(topBar = { TemporalTopBar("Ayarlar") }) { pad ->
    Column(
      Modifier
        .padding(pad)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

      Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Hedefler", style = MaterialTheme.typography.titleMedium)
          OutlinedTextField(bottle, { bottle = it.filter(Char::isDigit) }, label = { Text("Sise ml") }, singleLine = true)
          OutlinedTextField(target, { target = it.filter(Char::isDigit) }, label = { Text("Gunluk su hedefi (ml)") }, singleLine = true)
          OutlinedTextField(kcal, { kcal = it.filter(Char::isDigit) }, label = { Text("Gunluk kalori hedefi") }, singleLine = true)
          OutlinedTextField(carb, { carb = it.filter(Char::isDigit) }, label = { Text("Gunluk karbonhidrat hedefi (g)") }, singleLine = true)
          Button(onClick = {
            scope.launch {
              store.setWater(bottle.toIntOrNull() ?: 500, target.toIntOrNull() ?: 2500)
              store.setMacros(kcal.toIntOrNull() ?: 1800, carb.toIntOrNull() ?: 30)
            }
          }) { Text("Kaydet") }
        }
      }

      Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Moduller", style = MaterialTheme.typography.titleMedium)
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Su"); Switch(s.moduleFlags.water, { toggle("water", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Ogun"); Switch(s.moduleFlags.meals, { toggle("meals", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Aura"); Switch(s.moduleFlags.aura, { toggle("aura", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Uyku"); Switch(s.moduleFlags.sleep, { toggle("sleep", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Sigara"); Switch(s.moduleFlags.smoke, { toggle("smoke", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Ilac/Vit"); Switch(s.moduleFlags.intake, { toggle("intake", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Aktivite"); Switch(s.moduleFlags.activity, { toggle("activity", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Duygu"); Switch(s.moduleFlags.mood, { toggle("mood", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Saglik"); Switch(s.moduleFlags.health, { toggle("health", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Nobet"); Switch(s.moduleFlags.seizure, { toggle("seizure", it) }) }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Konum sor"); Switch(s.moduleFlags.location, { toggle("location", it) }) }
        }
      }

      Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Kisa yollar", style = MaterialTheme.typography.titleMedium)
          Button(onClick = { nav.navigate(Routes.SmokePlan) }, modifier = Modifier.fillMaxWidth()) {
            Text("Sigara planlayici")
          }
          Button(onClick = { nav.navigate(Routes.Intake) }, modifier = Modifier.fillMaxWidth()) {
            Text("Ilac / vitamin saat takibi")
          }
        }
      }

      Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Rapor", style = MaterialTheme.typography.titleMedium)
          Button(onClick = { nav.navigate(Routes.Export) }) { Text("PDF / Paylas") }
        }
      }
    }
  }
}
