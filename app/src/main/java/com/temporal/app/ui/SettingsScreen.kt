package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.SettingsStore
import com.temporal.app.ui.components.TemporalTopBar
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(nav: NavController) {
  val ctx = LocalContext.current
  val scope = rememberCoroutineScope()
  val store = remember { SettingsStore(ctx) }
  val s by store.flow.collectAsState(initial = com.temporal.app.data.AppSettings())

  var bottle by remember { mutableStateOf(s.bottleMl.toString()) }
  var target by remember { mutableStateOf(s.waterTargetMl.toString()) }
  var kcal by remember { mutableStateOf(s.dailyKcalTarget.toString()) }
  var carb by remember { mutableStateOf(s.dailyCarbTarget.toString()) }

  LaunchedEffect(s) {
    bottle = s.bottleMl.toString(); target = s.waterTargetMl.toString()
    kcal = s.dailyKcalTarget.toString(); carb = s.dailyCarbTarget.toString()
  }

  fun toggle(name:String, v:Boolean) { scope.launch { store.setModule(name, v) } }

  Scaffold(topBar = { TemporalTopBar("Ayarlar") }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

      Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Hedefler", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(bottle, { bottle = it.filter { c->c.isDigit() } }, label = { Text("Şişe ml") }, singleLine = true)
        OutlinedTextField(target, { target = it.filter { c->c.isDigit() } }, label = { Text("Günlük su hedefi (ml)") }, singleLine = true)
        OutlinedTextField(kcal, { kcal = it.filter { c->c.isDigit() } }, label = { Text("Günlük kalori hedefi") }, singleLine = true)
        OutlinedTextField(carb, { carb = it.filter { c->c.isDigit() } }, label = { Text("Günlük karbonhidrat hedefi (g)") }, singleLine = true)
        Button(onClick = {
          scope.launch {
            store.setWater(bottle.toIntOrNull() ?: 500, target.toIntOrNull() ?: 2500)
            store.setMacros(kcal.toIntOrNull() ?: 1800, carb.toIntOrNull() ?: 30)
          }
        }) { Text("Kaydet") }
      } }

      Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Modüller", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Su"); Switch(s.moduleFlags.water, { toggle("water", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Öğün"); Switch(s.moduleFlags.meals, { toggle("meals", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Aura"); Switch(s.moduleFlags.aura, { toggle("aura", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Uyku"); Switch(s.moduleFlags.sleep, { toggle("sleep", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Sigara"); Switch(s.moduleFlags.smoke, { toggle("smoke", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("İlaç/Vit"); Switch(s.moduleFlags.intake, { toggle("intake", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Aktivite"); Switch(s.moduleFlags.activity, { toggle("activity", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Duygu"); Switch(s.moduleFlags.mood, { toggle("mood", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Sağlık"); Switch(s.moduleFlags.health, { toggle("health", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Nöbet"); Switch(s.moduleFlags.seizure, { toggle("seizure", it) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Konum sor"); Switch(s.moduleFlags.location, { toggle("location", it) })
        }
      } }

      Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Rapor", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { nav.navigate(Routes.Export) }) { Text("PDF / Paylaş") }
      } }
    }
  }
}
