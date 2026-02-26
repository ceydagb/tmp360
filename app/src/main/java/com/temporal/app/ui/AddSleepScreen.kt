package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.SleepLog
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.ui.components.showDatePicker
import com.temporal.app.ui.components.showTimePicker
import com.temporal.app.util.Day
import com.temporal.app.util.TimeFmt
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun AddSleepScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()

  // default: yesterday
  val now = System.currentTimeMillis()
  var date by remember { mutableStateOf(Day.startOfDay(now - TimeUnit.DAYS.toMillis(1))) }
  var sleepTs by remember { mutableStateOf(date + 23*60*60*1000L) }
  var wakeTs by remember { mutableStateOf(date + 7*60*60*1000L) }
  var quality by remember { mutableStateOf(6f) }
  var notes by remember { mutableStateOf("") }

  Scaffold(topBar = { TemporalTopBar("Uyku Kaydı", onBack = { nav.popBackStack() }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("Gün (dün): ${TimeFmt.formatDate(date)}")
      Button(onClick = { showDatePicker(ctx, date) { d -> date = Day.startOfDay(d) } }, modifier = Modifier.fillMaxWidth()) { Text("Günü değiştir") }

      Text("Uyku: ${TimeFmt.format(sleepTs)}")
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { showTimePicker(ctx, sleepTs) { sleepTs = it } }, modifier = Modifier.weight(1f)) { Text("Uyku saati") }
        Button(onClick = { showTimePicker(ctx, wakeTs) { wakeTs = it } }, modifier = Modifier.weight(1f)) { Text("Uyanma saati") }
      }

      val durMin = ((wakeTs - sleepTs).coerceAtLeast(0L) / 60000L).toInt()
      Text("Süre: ${durMin/60} saat ${durMin%60} dk")

      Text("Kalite: ${quality.toInt()}/10")
      Slider(quality, { quality = it }, valueRange = 1f..10f, steps = 8)

      OutlinedTextField(notes, { notes = it }, label = { Text("Not") }, modifier = Modifier.fillMaxWidth())

      Button(onClick = {
        scope.launch {
          dao.upsertSleep(SleepLog(dateStart = date, sleepStart = sleepTs, wake = wakeTs, quality = quality.toInt(), notes = notes.ifBlank { null }))
          nav.popBackStack()
        }
      }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet") }
    }
  }
}
