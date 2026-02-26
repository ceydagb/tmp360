package com.temporal.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun SimpleBarChart(title: String, values: List<Int>, labels: List<String>) {
  Column(Modifier.fillMaxWidth()) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    val maxV = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Canvas(Modifier.fillMaxWidth().height(120.dp)) {
      val w = size.width
      val h = size.height
      val n = values.size.coerceAtLeast(1)
      val gap = w / (n * 2f)
      val barW = gap
      values.forEachIndexed { i, v ->
        val x = gap/2 + i*(barW+gap)
        val barH = (h * (v.toFloat()/maxV))
        drawLine(
          color = MaterialTheme.colorScheme.primary,
          start = Offset(x, h),
          end = Offset(x, h - barH),
          strokeWidth = barW,
          cap = StrokeCap.Round
        )
      }
    }
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      labels.take(7).forEach { Text(it, style = MaterialTheme.typography.labelSmall) }
    }
  }
}

@Composable
fun RiskRing(risk0to10: Float, subtitle: String) {
  val risk = risk0to10.coerceIn(0f,10f)
  val pct = risk/10f
  Column(Modifier.fillMaxWidth()) {
    Text("Risk", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Canvas(Modifier.size(140.dp)) {
      val stroke = 16f
      val r = min(size.width, size.height)/2 - stroke
      drawCircle(color = MaterialTheme.colorScheme.surfaceVariant, radius = r, style = Stroke(stroke))
      drawArc(
        color = MaterialTheme.colorScheme.tertiary,
        startAngle = -90f,
        sweepAngle = 360f*pct,
        useCenter = false,
        style = Stroke(stroke, cap = StrokeCap.Round)
      )
    }
    Spacer(Modifier.height(6.dp))
    Text("Skor: ${risk.toInt()}/10 • $subtitle", style = MaterialTheme.typography.bodyMedium)
  }
}
