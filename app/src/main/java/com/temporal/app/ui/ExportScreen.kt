package com.temporal.app.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.export.PdfExporter
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.util.Day
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun ExportScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()
  var status by remember { mutableStateOf("") }

  fun share(uri: android.net.Uri) {
    val i = Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(ctx, Intent.createChooser(i, "PDF paylaş"), null)
  }

  Scaffold(topBar = { TemporalTopBar("PDF Dışa Aktar", onBack = { nav.popBackStack() }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("Doktora sunmak için rapor üret.", style = MaterialTheme.typography.bodyMedium)
      if (status.isNotBlank()) Text(status)

      Button(onClick = {
        scope.launch {
          val now = System.currentTimeMillis()
          val from = now - TimeUnit.DAYS.toMillis(30)
          val seizures = dao.seizureCount(from, now)
          val water = dao.waterSum(from, now)
          val carbs = dao.carbSum(from, now)
          val lines = listOf(
            "Tarih aralığı: son 30 gün",
            "Toplam nöbet: $seizures",
            "Toplam su (ml): $water",
            "Toplam karbonhidrat (g): $carbs",
            "Not: Bu rapor takip amaçlıdır."
          )
          val uri = PdfExporter.exportSimpleReport(ctx, "Temporal Rapor", lines)
          status = "PDF hazır. Paylaşılıyor…"
          share(uri)
        }
      }, modifier = Modifier.fillMaxWidth()) { Text("Genel Rapor PDF") }
    }
  }
}
