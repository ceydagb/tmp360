package com.temporal.app.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.SmokeEventLog
import com.temporal.app.data.entities.SmokePlanSlot
import com.temporal.app.notifications.SmokeReminderReceiver
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.ui.components.showDatePicker
import com.temporal.app.ui.components.showTimePicker
import com.temporal.app.util.Day
import com.temporal.app.util.TimeFmt
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun SmokePlanScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()

  val now = System.currentTimeMillis()
  var monthStart by remember { mutableStateOf(Day.startOfDay(now)) }
  var selectedTs by remember { mutableStateOf(now) }
  var plannedSlots by remember { mutableStateOf(listOf<Long>()) }

  fun loadMonth() {
    scope.launch {
      val from = monthStart
      val to = from + TimeUnit.DAYS.toMillis(31)
      plannedSlots = dao.smokeSlotsBetween(from, to).map { it.ts }
    }
  }

  LaunchedEffect(monthStart) { loadMonth() }

  fun scheduleAlarm(ts: Long) {
    val am = ctx.getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager
    val i = Intent(ctx, SmokeReminderReceiver::class.java)
    val pi = PendingIntent.getBroadcast(ctx, ts.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ts, pi)
  }

  Scaffold(topBar = { TemporalTopBar("Sigara Planlayıcı", onBack = { nav.popBackStack() }) }) { pad ->
    LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

      item {
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("1 aylık plan (takvimden)", style = MaterialTheme.typography.titleMedium)
          Text("Seçili zaman: ${TimeFmt.format(selectedTs)}")
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { showDatePicker(ctx, selectedTs) { selectedTs = it } }, modifier = Modifier.weight(1f)) { Text("Tarih") }
            Button(onClick = { showTimePicker(ctx, selectedTs) { selectedTs = it } }, modifier = Modifier.weight(1f)) { Text("Saat") }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
              scope.launch { dao.upsertSmokeSlot(SmokePlanSlot(ts = selectedTs)); scheduleAlarm(selectedTs); loadMonth() }
            }, modifier = Modifier.weight(1f)) { Text("Bu zamanı ekle") }

            Button(onClick = {
              val from = monthStart
              val to = from + TimeUnit.DAYS.toMillis(31)
              scope.launch { dao.deleteSmokeSlotsBetween(from, to); plannedSlots = emptyList() }
            }, modifier = Modifier.weight(1f)) { Text("Bu ayı temizle") }
          }

          Spacer(Modifier.height(6.dp))
          Text("Bugün sigara içtin mi? (evet/hayır)", style = MaterialTheme.typography.titleMedium)
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { scope.launch { dao.insertSmokeEvent(SmokeEventLog(timestamp = System.currentTimeMillis(), smoked = true)) } }, modifier = Modifier.weight(1f)) { Text("Evet") }
            Button(onClick = { scope.launch { dao.insertSmokeEvent(SmokeEventLog(timestamp = System.currentTimeMillis(), smoked = false)) } }, modifier = Modifier.weight(1f)) { Text("Hayır") }
          }
        } }
      }

      item {
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Bu ay planlanan zamanlar", style = MaterialTheme.typography.titleMedium)
          if (plannedSlots.isEmpty()) Text("Henüz plan yok.")
          plannedSlots.take(30).forEach { Text("• " + TimeFmt.format(it)) }
        } }
      }

      item {
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("İyileşme (görsel) — v1", style = MaterialTheme.typography.titleMedium)
          Text("Son sigara üzerinden geçen süreye göre: 20dk/8s/24s/48s/2h/1g/3g/… adımlar bu ekranda genişletilecek.")
        } }
      }
    }
  }
}
