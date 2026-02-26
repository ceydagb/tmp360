package com.temporal.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ChartItem(val label: String, val value: Float) // 0..1 arası

@Composable
fun SimpleBarChart(title: String, items: List<ChartItem>) {
  Column(Modifier.fillMaxWidth()) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))

    items.forEach { it ->
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(it.label, style = MaterialTheme.typography.bodySmall)
        Text("${(it.value * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
      }
      LinearProgressIndicator(
        progress = { it.value.coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
      )
      Spacer(Modifier.height(10.dp))
    }
  }
}
