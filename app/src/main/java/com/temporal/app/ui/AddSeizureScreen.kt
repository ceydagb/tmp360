package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.SeizureLog
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.ui.components.showDatePicker
import com.temporal.app.ui.components.showTimePicker
import com.temporal.app.util.TimeFmt
import kotlinx.coroutines.launch

@Composable
fun AddSeizureScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()

  var ts by remember { mutableStateOf(System.currentTimeMillis()) }
  var durationSec by remember { mutableStateOf("60") }
  var consciousnessLoss by remember { mutableStateOf(true) }
  var postictal by remember { mutableStateOf("10") }

  // "önemli soru listesi" -> checkbox + detay
  val questions = listOf(
    "Öncesinde aura oldu mu?",
    "Uykusuzluk var mıydı?",
    "Aç kaldın mı / öğün atladın mı?",
    "Su az mıydı?",
    "Stres/duygu yoğun muydu?",
    "Kafein/nikotin değişimi oldu mu?",
    "Hastalık/ağrı var mıydı?",
    "İlaç saatinde gecikme oldu mu?"
  )
  val answers = remember { questions.associateWith { mutableStateOf(false) } }

  var place by remember { mutableStateOf("") }
  var contextTxt by remember { mutableStateOf("") }
  var triggers by remember { mutableStateOf("") }
  var symptoms by remember { mutableStateOf("Kasılma / bilinç değişimi / bakış sabitlenmesi vb.") }
  var notes by remember { mutableStateOf("") }

  Scaffold(topBar = { TemporalTopBar("Nöbet Geçirdim", onBack = { nav.popBackStack() }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text("Zaman: ${TimeFmt.format(ts)}")
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { showDatePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Tarih") }
        Button(onClick = { showTimePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Saat") }
      }

      OutlinedTextField(durationSec, { durationSec = it.filter(Char::isDigit) }, label = { Text("Süre (sn)") }, modifier = Modifier.fillMaxWidth())
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Bilinç kaybı"); Switch(consciousnessLoss, { consciousnessLoss = it })
      }
      OutlinedTextField(postictal, { postictal = it.filter(Char::isDigit) }, label = { Text("Postiktal süre (dk)") }, modifier = Modifier.fillMaxWidth())

      Text("Önemli sorular")
      questions.forEach { q ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(q, modifier = Modifier.weight(1f))
          Checkbox(answers[q]!!.value, { answers[q]!!.value = it })
        }
      }

      OutlinedTextField(place, { place = it }, label = { Text("Nerede?") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(contextTxt, { contextTxt = it }, label = { Text("Ortam / Ne yapıyordun?") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(triggers, { triggers = it }, label = { Text("Olası tetikleyici") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(symptoms, { symptoms = it }, label = { Text("Belirtiler") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(notes, { notes = it }, label = { Text("Not") }, modifier = Modifier.fillMaxWidth())

      Button(onClick = {
        val qSummary = questions.filter { answers[it]!!.value }.joinToString()
        val trig = listOf(triggers.trim(), qSummary).filter { it.isNotBlank() }.joinToString(" | ").ifBlank { null }
        scope.launch {
          dao.insertSeizure(
            SeizureLog(
              timestampStart = ts,
              durationSec = durationSec.toIntOrNull() ?: 0,
              consciousnessLoss = consciousnessLoss,
              postictalMin = postictal.toIntOrNull() ?: 0,
              place = place.ifBlank { null },
              context = contextTxt.ifBlank { null },
              triggers = trig,
              symptoms = symptoms,
              notes = notes.ifBlank { null }
            )
          )
          nav.popBackStack()
        }
      }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet") }
    }
  }
}
