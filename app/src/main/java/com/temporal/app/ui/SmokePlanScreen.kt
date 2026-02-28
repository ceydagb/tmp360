package com.temporal.app.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
  var selectedTs by remember { mutableLongStateOf(now) }
  var lastSmokedTs by remember { mutableLongStateOf(now) }
  var intervalMinText by remember { mutableStateOf("90") }
  var planDaysText by remember { mutableStateOf("1") }
  var useLastSmokedBase by remember { mutableStateOf(true) }
  var plannedSlots by remember { mutableStateOf(listOf<Long>()) }

  fun loadPlannedWindow() {
    scope.launch {
      val from = Day.startOfDay(System.currentTimeMillis())
      val to = from + TimeUnit.DAYS.toMillis(35)
      plannedSlots = dao.smokeSlotsBetween(from, to).map { it.ts }
    }
  }

  LaunchedEffect(Unit) {
    loadPlannedWindow()
    val latest = dao.lastSmokedAt()
    if (latest != null) {
      lastSmokedTs = latest
      selectedTs = latest
    }
  }

  fun scheduleAlarm(ts: Long) {
    val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val i = Intent(ctx, SmokeReminderReceiver::class.java).apply {
      putExtra("title", "Sigara hatirlatici")
      putExtra("text", "Planlanan icim zamani geldi.")
      putExtra("route", Routes.SmokePlan)
    }
    val pi = PendingIntent.getBroadcast(
      ctx,
      ts.hashCode(),
      i,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ts, pi)
  }

  fun generateSlots(baseTs: Long, intervalMin: Int, days: Int): List<Long> {
    if (intervalMin <= 0 || days <= 0) return emptyList()
    val intervalMs = TimeUnit.MINUTES.toMillis(intervalMin.toLong())
    val end = baseTs + TimeUnit.DAYS.toMillis(days.toLong())
    val out = mutableListOf<Long>()
    var t = baseTs + intervalMs
    while (t <= end) {
      out += t
      t += intervalMs
    }
    return out
  }

  Scaffold(topBar = { TemporalTopBar("Sigara Planlayici", onBack = { nav.popBackStack() }) }) { pad ->
    LazyColumn(
      modifier = Modifier
        .padding(pad)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Card {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Son icilen sigara", style = MaterialTheme.typography.titleMedium)
            Text("Son kayit: ${TimeFmt.format(lastSmokedTs)}")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Button(
                onClick = {
                  scope.launch {
                    val ts = System.currentTimeMillis()
                    dao.insertSmokeEvent(SmokeEventLog(timestamp = ts, smoked = true))
                    lastSmokedTs = ts
                    loadPlannedWindow()
                  }
                },
                modifier = Modifier.weight(1f)
              ) { Text("Simdi ictim") }
              Button(
                onClick = { scope.launch { dao.insertSmokeEvent(SmokeEventLog(timestamp = System.currentTimeMillis(), smoked = false)) } },
                modifier = Modifier.weight(1f)
              ) { Text("Icmedim") }
            }
          }
        }
      }

      item {
        Card {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Dakika araligi ile plan", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Baz: son icilen sigara")
              Switch(checked = useLastSmokedBase, onCheckedChange = { useLastSmokedBase = it })
            }

            if (!useLastSmokedBase) {
              Text("Secili baz zaman: ${TimeFmt.format(selectedTs)}")
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { showDatePicker(ctx, selectedTs) { selectedTs = it } }, modifier = Modifier.weight(1f)) { Text("Tarih") }
                Button(onClick = { showTimePicker(ctx, selectedTs) { selectedTs = it } }, modifier = Modifier.weight(1f)) { Text("Saat") }
              }
            }

            OutlinedNumberField(
              value = intervalMinText,
              onValue = { intervalMinText = it },
              label = "Kac dakikada bir?"
            )
            OutlinedNumberField(
              value = planDaysText,
              onValue = { planDaysText = it },
              label = "Kac gun planlansin? (1=yarin, 30=aylik)"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Button(
                onClick = {
                  val interval = intervalMinText.toIntOrNull() ?: 0
                  val days = (planDaysText.toIntOrNull() ?: 0).coerceAtMost(31)
                  val base = if (useLastSmokedBase) lastSmokedTs else selectedTs
                  val slots = generateSlots(base, interval, days)
                  if (slots.isEmpty()) return@Button

                  scope.launch {
                    val from = Day.startOfDay(base)
                    val to = from + TimeUnit.DAYS.toMillis(days.toLong() + 1)
                    dao.deleteSmokeSlotsBetween(from, to)
                    slots.forEach {
                      dao.upsertSmokeSlot(SmokePlanSlot(ts = it))
                      scheduleAlarm(it)
                    }
                    loadPlannedWindow()
                  }
                },
                modifier = Modifier.weight(1f)
              ) { Text("Plani olustur") }

              Button(
                onClick = {
                  scope.launch {
                    val from = Day.startOfDay(System.currentTimeMillis())
                    val to = from + TimeUnit.DAYS.toMillis(35)
                    dao.deleteSmokeSlotsBetween(from, to)
                    plannedSlots = emptyList()
                  }
                },
                modifier = Modifier.weight(1f)
              ) { Text("Plani temizle") }
            }
          }
        }
      }

      item {
        Card {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Planlanan hatirlatmalar", style = MaterialTheme.typography.titleMedium)
            val upcoming = plannedSlots.filter { it >= System.currentTimeMillis() }
            if (upcoming.isEmpty()) Text("Plan bulunmuyor.")
            if (upcoming.isNotEmpty()) {
              Text("Siradaki: ${TimeFmt.format(upcoming.first())}")
            }
            upcoming.take(40).forEach { Text("- ${TimeFmt.format(it)}") }
          }
        }
      }
    }
  }
}

@Composable
private fun OutlinedNumberField(value: String, onValue: (String) -> Unit, label: String) {
  androidx.compose.material3.OutlinedTextField(
    value = value,
    onValueChange = { onValue(it.filter(Char::isDigit)) },
    label = { Text(label) },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true
  )
}
