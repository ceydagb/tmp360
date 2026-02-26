package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.AuraLog
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.ui.components.showDatePicker
import com.temporal.app.ui.components.showTimePicker
import com.temporal.app.util.TimeFmt
import kotlinx.coroutines.launch

@Composable
fun AddAuraScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()

  var ts by remember { mutableStateOf(System.currentTimeMillis()) }
  val types = listOf("Baş dönmesi", "Yabancılaşma", "Görsel değişim", "Koku/tat", "Mide hissi", "Diğer")
  var type by remember { mutableStateOf(types.first()) }
  var intensity by remember { mutableStateOf(5f) }
  var notes by remember { mutableStateOf("") }

  Scaffold(topBar = { TemporalTopBar("Aura Kaydı", onBack = { nav.popBackStack() }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("Zaman: ${TimeFmt.format(ts)}")
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { showDatePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Tarih") }
        Button(onClick = { showTimePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Saat") }
      }

      var exp by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
        OutlinedTextField(type, {}, readOnly = true, label = { Text("Belirti türü") }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
          types.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; exp = false }) }
        }
      }

      Text("Şiddet: ${intensity.toInt()}/10")
      Slider(value = intensity, onValueChange = { intensity = it }, valueRange = 1f..10f, steps = 8)

      OutlinedTextField(notes, { notes = it }, label = { Text("Not") }, modifier = Modifier.fillMaxWidth())

      Button(onClick = {
        scope.launch {
          dao.insertAura(AuraLog(timestamp = ts, auraType = type, intensity = intensity.toInt(), notes = notes.ifBlank { null }))
          nav.popBackStack()
        }
      }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet") }
    }
  }
}
