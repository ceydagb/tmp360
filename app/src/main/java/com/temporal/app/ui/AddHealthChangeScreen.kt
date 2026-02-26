package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.HealthChangeLog
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.ui.components.showDatePicker
import com.temporal.app.ui.components.showTimePicker
import com.temporal.app.util.TimeFmt
import kotlinx.coroutines.launch

@Composable
fun AddHealthChangeScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()

  var ts by remember { mutableStateOf(System.currentTimeMillis()) }
  val cats = listOf("Migren", "İshal", "Ateş", "Yara/Bere", "Bulantı", "Ağrı", "Soğuk algınlığı", "Diğer")
  var cat by remember { mutableStateOf(cats.first()) }
  var severity by remember { mutableStateOf(5f) }
  var notes by remember { mutableStateOf("") }

  Scaffold(topBar = { TemporalTopBar("Sağlık Değişikliği", onBack = { nav.popBackStack() }) }) { pad ->
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
          cats.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { cat = c; exp = false }) }
        }
      }

      Text("Şiddet: ${severity.toInt()}/10")
      Slider(severity, { severity = it }, valueRange = 1f..10f, steps = 8)
      OutlinedTextField(notes, { notes = it }, label = { Text("Not") }, modifier = Modifier.fillMaxWidth())

      Button(onClick = {
        scope.launch {
          dao.insertHealth(HealthChangeLog(timestamp = ts, category = cat, severity = severity.toInt(), notes = notes.ifBlank { null }))
          nav.popBackStack()
        }
      }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet") }
    }
  }
}
