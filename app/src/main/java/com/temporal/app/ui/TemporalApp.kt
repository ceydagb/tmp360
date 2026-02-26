package com.temporal.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*

@Composable
fun TemporalApp(initialRoute: String? = null) {
  val nav = rememberNavController()
  val start = initialRoute ?: Routes.Dashboard

  Scaffold(
    bottomBar = {
      NavigationBar {
        val dest = nav.currentBackStackEntryAsState().value?.destination?.route

        fun item(route: String, label: String, icon: @Composable () -> Unit) {
          NavigationBarItem(
            selected = dest == route,
            onClick = { nav.navigate(route) { launchSingleTop = true } },
            icon = icon,
            label = { Text(label) }
          )
        }

        item(Routes.Dashboard, "Ana") { Icon(Icons.Default.Home, null) }
        item(Routes.Analysis, "Analiz") { Icon(Icons.Default.Insights, null) }
        item(Routes.Intake, "İlaç/Vit.") { Icon(Icons.Default.Medication, null) }
        item(Routes.Settings, "Ayarlar") { Icon(Icons.Default.Settings, null) }
      }
    }
  ) { pad ->
    NavHost(
      navController = nav,
      startDestination = start,
      modifier = Modifier.padding(pad)
    ) {
      composable(Routes.Dashboard) { DashboardScreen(nav) }
      composable(Routes.Analysis) { AnalysisScreen(nav) }
      composable(Routes.Settings) { SettingsScreen(nav) }

      composable(Routes.AddWater) { AddWaterScreen(nav) }
      composable(Routes.AddMeal) { AddMealScreen(nav) }
      composable(Routes.AddFood) { AddFoodScreen(nav) }

      composable(Routes.SmokePlan) { SmokePlanScreen(nav) }

      composable(Routes.AddAura) { AddAuraScreen(nav) }
      composable(Routes.AddSleep) { AddSleepScreen(nav) }

      composable(Routes.Intake) { IntakeScreen(nav) }

      composable(Routes.AddMood) { AddMoodScreen(nav) }
      composable(Routes.AddActivity) { AddActivityScreen(nav) }
      composable(Routes.AddHealth) { AddHealthChangeScreen(nav) }
      composable(Routes.AddSeizure) { AddSeizureScreen(nav) }

      composable(Routes.Export) { ExportScreen(nav) }
    }
  }
}
