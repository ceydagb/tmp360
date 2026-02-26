package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.SettingsStore
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.WaterLog
import com.temporal.app.ui.components.TemporalTopBar
import kotlinx.coroutines.launch

@Composable
fun AddWaterScreen(nav: NavController) {
  val ctx = LocalContext.current
  val scope = rememberCoroutineScope()
  val store = remember { SettingsStore(ctx) }
  var bottle by remember { mutableStateOf(500) }
  LaunchedEffect(Unit) { bottle = store.get().bottleMl }

  Scaffold(topBar = { TemporalTopBar("Su Ekle", onBack = { nav.popBackStack() }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("1 şişe = ${bottle}ml (Ayarlar’dan değişir)")
      Button(onClick = {
        scope.launch {
          TemporalDb.get(ctx).dao().insertWater(WaterLog(timestamp = System.currentTimeMillis(), amountMl = bottle))
          nav.popBackStack()
        }
      }) { Text("Şişemi bitirdim") }
    }
  }
}
