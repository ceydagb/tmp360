package com.temporal.app.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.temporal.app.data.entities.IntakeLog
import com.temporal.app.data.entities.MedItem
import com.temporal.app.notifications.MedicationReminderReceiver
import com.temporal.app.ui.components.TemporalTopBar
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun IntakeScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()
  val meds by dao.meds().collectAsState(initial = emptyList())

  var type by remember { mutableStateOf("Ilac") }
  val types = listOf("Ilac", "Vitamin", "Takviye")
  var name by remember { mutableStateOf("") }
  var mgPer by remember { mutableStateOf("") }
  var count by remember { mutableStateOf("1") }
  var remindAfterHours by remember { mutableStateOf("0") }

  var selected by remember { mutableStateOf<MedItem?>(null) }
  var exp by remember { mutableStateOf(false) }

  fun scheduleMedicationReminder(item: MedItem, hours: Int) {
    if (hours <= 0) return
    val triggerAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours.toLong())
    val am = ctx.getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager
    val i = Intent(ctx, MedicationReminderReceiver::class.java).apply {
      putExtra("title", "Ilac saati")
      putExtra("text", "${item.name} icin ${hours} saat sonraki hatirlatici.")
      putExtra("route", Routes.Intake)
    }
    val requestCode = (item.id * 1000 + triggerAt % 1000).toInt()
    val pi = PendingIntent.getBroadcast(
      ctx,
      requestCode,
      i,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
  }

  Scaffold(topBar = {
    TemporalTopBar("Ilac / Vitamin / Takviye", actions = {
      TextButton(onClick = { nav.navigate(Routes.Export) }) { Text("PDF") }
    })
  }) { pad ->
    LazyColumn(
      Modifier
        .padding(pad)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

      item {
        Card {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tanim ekle (5+ destekli)", style = MaterialTheme.typography.titleMedium)

            var texp by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = texp, onExpandedChange = { texp = !texp }) {
              OutlinedTextField(
                value = type,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tur") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = texp) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth()
              )
              ExposedDropdownMenu(expanded = texp, onDismissRequest = { texp = false }) {
                types.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; texp = false }) }
              }
            }

            OutlinedTextField(name, { name = it }, label = { Text("Ad") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(mgPer, { mgPer = it.filter(Char::isDigit) }, label = { Text("Doz (mg)") }, modifier = Modifier.fillMaxWidth())

            Button(onClick = {
              val n = name.trim()
              if (n.isBlank()) return@Button
              scope.launch {
                dao.upsertMed(MedItem(type = type, name = n, mgPerDose = mgPer.toIntOrNull() ?: 0))
                name = ""
                mgPer = ""
              }
            }, modifier = Modifier.fillMaxWidth()) { Text("Tanimi Kaydet") }
          }
        }
      }

      item {
        Card {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Alim kaydi", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
              OutlinedTextField(
                value = selected?.let { "${it.type}: ${it.name} (${it.mgPerDose}mg)" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Sec") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exp) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth()
              )
              ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                meds.forEach { m ->
                  DropdownMenuItem(
                    text = { Text("${m.type}: ${m.name} (${m.mgPerDose}mg)") },
                    onClick = { selected = m; exp = false }
                  )
                }
              }
            }

            OutlinedTextField(count, { count = it.filter(Char::isDigit) }, label = { Text("Adet") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
              remindAfterHours,
              { remindAfterHours = it.filter(Char::isDigit) },
              label = { Text("Kac saat sonra hatirlatilsin? (0=kapali)") },
              modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
              val m = selected ?: return@Button
              val c = count.toIntOrNull() ?: 1
              val h = remindAfterHours.toIntOrNull() ?: 0
              scope.launch {
                dao.insertIntake(
                  IntakeLog(
                    timestamp = System.currentTimeMillis(),
                    itemId = m.id,
                    type = m.type,
                    mg = m.mgPerDose,
                    count = c
                  )
                )
                scheduleMedicationReminder(m, h)
                count = "1"
              }
            }, modifier = Modifier.fillMaxWidth()) { Text("Aldim") }
          }
        }
      }
    }
  }
}
