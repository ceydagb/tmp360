package com.temporal.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.temporal.app.MainActivity
import com.temporal.app.R
import com.temporal.app.ui.Routes

class TemporalWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    appWidgetIds.forEach { id ->
      val rv = RemoteViews(context.packageName, R.layout.widget_temporal)

      fun bind(btnId:Int, route:String) {
        val i = Intent(context, MainActivity::class.java).apply { putExtra("route", route) }
        val pi = PendingIntent.getActivity(context, (btnId*31+route.hashCode()), i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        rv.setOnClickPendingIntent(btnId, pi)
      }

      bind(R.id.btnWater, Routes.AddWater)
      bind(R.id.btnMeal, Routes.AddMeal)
      bind(R.id.btnSeizure, Routes.AddSeizure)
      bind(R.id.btnSmoke, Routes.SmokePlan)
      bind(R.id.btnAura, Routes.AddAura)
      bind(R.id.btnIntake, Routes.Intake)

      appWidgetManager.updateAppWidget(id, rv)
    }
  }
}
