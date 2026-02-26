package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.IntakeLog
import com.temporal.app.data.entities.MedItem
import com.temporal.app.ui.components.TemporalTopBar
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun IntakeScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()
  val meds by dao.meds().collectAsState(initial = emptyList())

  var type by remember { mutableStateOf("İlaç") } // İlaç / Vitamin / Takviye
  val types = listOf("İlaç","Vitamin","Takviye")
  var name by remember { mutableStateOf("") }
  var mgPer by remember { mutableStateOf("") }
  var count by remember { mutableStateOf("1") }

  var selected by remember { mutableStateOf<MedItem?>(null) }
  var exp by remember { mutableStateOf(false) }

  Scaffold(topBar = { TemporalTopBar("İlaç / Vitamin / Takviye", actions = {
    TextButton(onClick = { nav.navigate(Routes.Export) }) { Text("PDF") }
  }) }) { pad ->
    LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

      item {
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Tanım ekle (5+ destekli)", style = MaterialTheme.typography.titleMedium)

          var texp by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(expanded = texp, onExpandedChange = { texp = !texp }) {
            OutlinedTextField(type, {}, readOnly = true, label = { Text("Tür") }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = texp, onDismissRequest = { texp = false }) {
              types.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; texp = false }) }
            }
          }

          OutlinedTextField(name, { name = it }, label = { Text("Ad") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(mgPer, { mgPer = it.filter(Char::isDigit) }, label = { Text("Doz (mg)") }, modifier = Modifier.fillMaxWidth())

          Button(onClick = {
            val n = name.trim(); if (n.isBlank()) return@Button
            scope.launch { dao.upsertMed(MedItem(type = type, name = n, mgPerDose = mgPer.toIntOrNull() ?: 0)); name=""; mgPer="" }
          }, modifier = Modifier.fillMaxWidth()) { Text("Tanımı Kaydet") }
        } }
      }

      item {
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Alım kaydı", style = MaterialTheme.typography.titleMedium)

          ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
            OutlinedTextField(
              value = selected?.let { "${it.type}: ${it.name} (${it.mgPerDose}mg)" } ?: "",
              onValueChange = {},
              readOnly = true,
              label = { Text("Seç") },
              modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
              meds.forEach { m ->
                DropdownMenuItem(text = { Text("${m.type}: ${m.name} (${m.mgPerDose}mg)") }, onClick = { selected = m; exp = false })
              }
            }
          }

          OutlinedTextField(count, { count = it.filter(Char::isDigit) }, label = { Text("Adet") }, modifier = Modifier.fillMaxWidth())
          Button(onClick = {
            val m = selected ?: return@Button
            scope.launch {
              dao.insertIntake(IntakeLog(timestamp = System.currentTimeMillis(), itemId = m.id, type = m.type, mg = m.mgPerDose, count = count.toIntOrNull() ?: 1))
              count = "1"
            }
          }, modifier = Modifier.fillMaxWidth()) { Text("Aldım") }
        } }
      }
    }
  }
}
