package com.temporal.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.temporal.app.data.dao.TemporalDao
import com.temporal.app.data.entities.*

@Database(
  entities = [
    WaterLog::class, FoodItem::class, MealLog::class, AuraLog::class, SleepLog::class,
    MedItem::class, IntakeLog::class, ActivityLog::class, MoodLog::class, HealthChangeLog::class,
    SeizureLog::class, SmokeEventLog::class, SmokePlanSlot::class, PlaceCheckInLog::class
  ],
  version = 1,
  exportSchema = false
)
abstract class TemporalDb : RoomDatabase() {
  abstract fun dao(): TemporalDao

  companion object {
    @Volatile private var I: TemporalDb? = null
    fun get(context: Context): TemporalDb =
      I ?: synchronized(this) {
        I ?: Room.databaseBuilder(context.applicationContext, TemporalDb::class.java, "temporal.db")
          .fallbackToDestructiveMigration()
          .build().also { I = it }
      }
  }
}
