package com.temporal.app.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

  fun exportSimpleReport(context: Context, title: String, lines: List<String>): android.net.Uri {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = doc.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint().apply { textSize = 14f }
    var y = 48f
    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText(title, 32f, y, paint)
    y += 28f
    paint.textSize = 14f
    paint.isFakeBoldText = false

    lines.forEach { line ->
      if (y > 800f) return@forEach
      canvas.drawText(line, 32f, y, paint)
      y += 20f
    }

    doc.finishPage(page)

    val outDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(outDir, "Temporal_Rapor_${System.currentTimeMillis()}.pdf")
    FileOutputStream(file).use { doc.writeTo(it) }
    doc.close()

    return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
  }
}
