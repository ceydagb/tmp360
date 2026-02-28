package com.temporal.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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

  val questions = listOf(
    "Oncesinde aura oldu mu?",
    "Uykusuzluk var miydi?",
    "Ac kaldin mi / ogun atladin mi?",
    "Su az miydi?",
    "Stres/duygu yogun muydu?",
    "Kafein/nikotin degisimi oldu mu?",
    "Hastalik/agri var miydi?",
    "Ilac saatinde gecikme oldu mu?"
  )
  val answers = remember { questions.associateWith { mutableStateOf(false) } }

  var place by remember { mutableStateOf("") }
  var contextTxt by remember { mutableStateOf("") }
  var triggers by remember { mutableStateOf("") }
  var symptoms by remember { mutableStateOf("Kasilma / bilinc degisimi / bakis sabitlenmesi vb.") }
  var notes by remember { mutableStateOf("") }

  Scaffold(topBar = { TemporalTopBar("Nobet Gecirdim", onBack = { nav.popBackStack() }) }) { pad ->
    Column(
      modifier = Modifier
        .padding(pad)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text("Zaman: ${TimeFmt.format(ts)}")
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { showDatePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Tarih") }
        Button(onClick = { showTimePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Saat") }
      }

      OutlinedTextField(durationSec, { durationSec = it.filter(Char::isDigit) }, label = { Text("Sure (sn)") }, modifier = Modifier.fillMaxWidth())
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Bilinc kaybi")
        Switch(consciousnessLoss, { consciousnessLoss = it })
      }
      OutlinedTextField(postictal, { postictal = it.filter(Char::isDigit) }, label = { Text("Postiktal sure (dk)") }, modifier = Modifier.fillMaxWidth())

      Text("Onemli sorular")
      questions.forEach { q ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(q, modifier = Modifier.weight(1f))
          Checkbox(answers[q]!!.value, { answers[q]!!.value = it })
        }
      }

      OutlinedTextField(place, { place = it }, label = { Text("Nerede?") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(contextTxt, { contextTxt = it }, label = { Text("Ortam / Ne yapiyordun?") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(triggers, { triggers = it }, label = { Text("Olasi tetikleyici") }, modifier = Modifier.fillMaxWidth())
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
