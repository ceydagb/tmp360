package com.temporal.app.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.PlaceCheckInLog
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.ui.components.showDatePicker
import com.temporal.app.ui.components.showTimePicker
import kotlinx.coroutines.launch

private val placePresets = listOf("Ev", "İş", "Dışarı", "AVM", "Yürüyüş", "Market", "Misafirlik", "Hastane", "Diğer")

@SuppressLint("MissingPermission")
@Composable
fun AddPlaceScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()

  var ts by remember { mutableLongStateOf(System.currentTimeMillis()) }
  var place by remember { mutableStateOf("Ev") }
  var custom by remember { mutableStateOf("") }
  var lat by remember { mutableStateOf<Double?>(null) }
  var lon by remember { mutableStateOf<Double?>(null) }
  var status by remember { mutableStateOf("") }

  fun effectivePlace(): String = if (place == "Diğer") custom.trim().ifBlank { "Diğer" } else place

  Scaffold(topBar = { TemporalTopBar("Konum / Nerede?", onBack = { nav.popBackStack() }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { showDatePicker(ctx, ts) { ts = it } }) { Text("Tarih") }
        OutlinedButton(onClick = { showTimePicker(ctx, ts) { ts = it } }) { Text("Saat") }
      }

      Text("Seç:", style = MaterialTheme.typography.titleSmall)
      placePresets.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          row.forEach { p ->
            FilterChip(
              selected = place == p,
              onClick = { place = p },
              label = { Text(p) }
            )
          }
        }
      }
      if (place == "Diğer") {
        OutlinedTextField(value = custom, onValueChange = { custom = it }, label = { Text("Açıklama") }, modifier = Modifier.fillMaxWidth())
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
          onClick = {
            status = "Konum alınıyor…"
            val client = LocationServices.getFusedLocationProviderClient(ctx)
            client.lastLocation.addOnSuccessListener { loc ->
              if (loc != null) { lat = loc.latitude; lon = loc.longitude; status = "Konum alındı." }
              else status = "Konum alınamadı (GPS kapalı olabilir)."
            }.addOnFailureListener { status = "Konum hatası: ${'$'}{it.message}" }
          },
          modifier = Modifier.weight(1f)
        ) { Text("Konumu al") }

        Button(
          onClick = {
            scope.launch {
              dao.insertPlace(PlaceCheckInLog(timestamp = ts, placeText = effectivePlace(), lat = lat, lon = lon))
              nav.popBackStack()
            }
          },
          modifier = Modifier.weight(1f)
        ) { Text("Kaydet") }
      }

      if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall)
      Text("Not: Konum izni verirsen “Konumu al” daha doğru çalışır.", style = MaterialTheme.typography.bodySmall)
    }
  }
}
