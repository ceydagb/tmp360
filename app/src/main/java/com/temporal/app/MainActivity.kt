package com.temporal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.temporal.app.data.seed.SeedData
import com.temporal.app.notifications.Scheduler
import com.temporal.app.ui.TemporalApp
import com.temporal.app.ui.theme.TemporalTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    SeedData.ensure(this)
    Scheduler.ensureAll(this)

    val route = intent?.getStringExtra("route")
    setContent { TemporalTheme { TemporalApp(initialRoute = route) } }
  }

  override fun onNewIntent(intent: android.content.Intent?) {
    super.onNewIntent(intent); setIntent(intent)
  }
}
