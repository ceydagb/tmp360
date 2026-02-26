package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.MoodLog
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.ui.components.showDatePicker
import com.temporal.app.ui.components.showTimePicker
import com.temporal.app.util.TimeFmt
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddMoodScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()

  var ts by remember { mutableStateOf(System.currentTimeMillis()) }
  val mains = listOf("Mutlu","Üzgün","Sinirli","Kaygılı","Heyecanlı","Ağlama","Gülme","Yorgun","Diğer")
  var main by remember { mutableStateOf(mains.first()) }
  val reasons = mapOf(
    "Sinirli" to listOf("İş", "Aile", "Trafik", "Yorgunluk", "Diğer"),
    "Üzgün" to listOf("Haber", "Yalnızlık", "Ağrı", "Diğer"),
    "Kaygılı" to listOf("Sağlık", "İş", "Gelecek", "Diğer"),
    "Mutlu" to listOf("Başarı", "Sosyal", "Dinlenme", "Diğer")
  )
  var sub by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  Scaffold(topBar = { TemporalTopBar("Duygu Durum", onBack = { nav.popBackStack() }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("Zaman: ${TimeFmt.format(ts)}")
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { showDatePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Tarih") }
        Button(onClick = { showTimePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Saat") }
      }

      var exp by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
        OutlinedTextField(main, {}, readOnly = true, label = { Text("Duygu") }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
          mains.forEach { m -> DropdownMenuItem(text = { Text(m) }, onClick = { main = m; sub = ""; exp = false }) }
        }
      }

      val subs = reasons[main] ?: emptyList()
      if (subs.isNotEmpty()) {
        var sexp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = sexp, onExpandedChange = { sexp = !sexp }) {
          OutlinedTextField(sub, {}, readOnly = true, label = { Text("Sebep") }, modifier = Modifier.menuAnchor().fillMaxWidth())
          ExposedDropdownMenu(expanded = sexp, onDismissRequest = { sexp = false }) {
            subs.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { sub = s; sexp = false }) }
          }
        }
      }

      OutlinedTextField(notes, { notes = it }, label = { Text("Not") }, modifier = Modifier.fillMaxWidth())

      Button(onClick = {
        scope.launch {
          dao.insertMood(MoodLog(timestamp = ts, moodMain = main, moodSub = sub.ifBlank { null }, notes = notes.ifBlank { null }))
          nav.popBackStack()
        }
      }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet") }
    }
  }
}
