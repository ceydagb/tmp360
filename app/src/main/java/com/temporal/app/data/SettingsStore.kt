package com.temporal.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("temporal_settings")

data class ModuleFlags(
  val water:Boolean=true, val meals:Boolean=true, val aura:Boolean=true, val sleep:Boolean=true,
  val smoke:Boolean=true, val intake:Boolean=true, val activity:Boolean=true, val mood:Boolean=true,
  val health:Boolean=true, val seizure:Boolean=true, val location:Boolean=true
)

data class AppSettings(
  val bottleMl:Int=500,
  val waterTargetMl:Int=2500,
  val dailyKcalTarget:Int=1800,
  val dailyCarbTarget:Int=30,
  val moduleFlags: ModuleFlags = ModuleFlags()
)

class SettingsStore(private val context: Context) {
  private object K {
    val BOTTLE = intPreferencesKey("bottle")
    val WATER_TARGET = intPreferencesKey("water_target")
    val KCAL = intPreferencesKey("kcal")
    val CARB = intPreferencesKey("carb")

    fun m(name:String) = booleanPreferencesKey("m_$name")
  }

  val flow: Flow<AppSettings> = context.ds.data.map { p ->
    val mf = ModuleFlags(
      water = p[K.m("water")] ?: true,
      meals = p[K.m("meals")] ?: true,
      aura = p[K.m("aura")] ?: true,
      sleep = p[K.m("sleep")] ?: true,
      smoke = p[K.m("smoke")] ?: true,
      intake = p[K.m("intake")] ?: true,
      activity = p[K.m("activity")] ?: true,
      mood = p[K.m("mood")] ?: true,
      health = p[K.m("health")] ?: true,
      seizure = p[K.m("seizure")] ?: true,
      location = p[K.m("location")] ?: true
    )
    AppSettings(
      bottleMl = p[K.BOTTLE] ?: 500,
      waterTargetMl = p[K.WATER_TARGET] ?: 2500,
      dailyKcalTarget = p[K.KCAL] ?: 1800,
      dailyCarbTarget = p[K.CARB] ?: 30,
      moduleFlags = mf
    )
  }

  suspend fun get() = flow.first()

  suspend fun setWater(bottleMl:Int, targetMl:Int) = context.ds.edit { p -> p[K.BOTTLE]=bottleMl; p[K.WATER_TARGET]=targetMl }
  suspend fun setMacros(kcal:Int, carb:Int) = context.ds.edit { p -> p[K.KCAL]=kcal; p[K.CARB]=carb }
  suspend fun setModule(name:String, enabled:Boolean) = context.ds.edit { p -> p[K.m(name)] = enabled }
}
