package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.ActivityLog
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.ui.components.showDatePicker
import com.temporal.app.ui.components.showTimePicker
import com.temporal.app.util.TimeFmt
import kotlinx.coroutines.launch

@Composable
fun AddActivityScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()

  var ts by remember { mutableStateOf(System.currentTimeMillis()) }
  val preset = listOf("Yürüyüş", "Temizlik", "AVM", "Market", "Merdiven", "Spor", "Oturuyorum", "Diğer")
  var cat by remember { mutableStateOf(preset.first()) }
  var duration by remember { mutableStateOf("30") }
  var notes by remember { mutableStateOf("") }

  Scaffold(topBar = { TemporalTopBar("Fiziksel Aktivite", onBack = { nav.popBackStack() }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("Zaman: ${TimeFmt.format(ts)}")
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { showDatePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Tarih") }
        Button(onClick = { showTimePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Saat") }
      }

      var exp by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
        OutlinedTextField(cat, {}, readOnly = true, label = { Text("Kategori") }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
          preset.forEach { p -> DropdownMenuItem(text = { Text(p) }, onClick = { cat = p; exp = false }) }
        }
      }

      OutlinedTextField(duration, { duration = it.filter(Char::isDigit) }, label = { Text("Süre (dk)") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(notes, { notes = it }, label = { Text("Not / Detay") }, modifier = Modifier.fillMaxWidth())

      Button(onClick = {
        scope.launch {
          dao.insertActivity(ActivityLog(timestamp = ts, category = cat, durationMin = duration.toIntOrNull() ?: 0, notes = notes.ifBlank { null }))
          nav.popBackStack()
        }
      }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet") }
    }
  }
}
