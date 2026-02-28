package com.temporal.app.data.dao

import androidx.room.*
import com.temporal.app.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TemporalDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertWater(x: WaterLog)
  @Query("SELECT * FROM WaterLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC") fun waterBetween(from:Long,to:Long): Flow<List<WaterLog>>
  @Query("SELECT COALESCE(SUM(amountMl),0) FROM WaterLog WHERE timestamp BETWEEN :from AND :to") suspend fun waterSum(from:Long,to:Long): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertFood(x: FoodItem): Long
  @Query("SELECT * FROM FoodItem ORDER BY category, name") fun foods(): Flow<List<FoodItem>>
  @Query("SELECT * FROM FoodItem WHERE category=:cat ORDER BY name") fun foodsByCat(cat:String): Flow<List<FoodItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMeal(x: MealLog)
  @Query("SELECT * FROM MealLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC") fun mealsBetween(from:Long,to:Long): Flow<List<MealLog>>
  @Query("SELECT COALESCE(SUM(kcal),0) FROM MealLog WHERE timestamp BETWEEN :from AND :to") suspend fun kcalSum(from:Long,to:Long): Int
  @Query("SELECT COALESCE(SUM(carbsG),0) FROM MealLog WHERE timestamp BETWEEN :from AND :to") suspend fun carbSum(from:Long,to:Long): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAura(x: AuraLog)
  @Query("SELECT * FROM AuraLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC") fun auraBetween(from:Long,to:Long): Flow<List<AuraLog>>
  @Query("SELECT auraType, COUNT(*) as c FROM AuraLog WHERE timestamp BETWEEN :from AND :to GROUP BY auraType ORDER BY c DESC") suspend fun auraCounts(from:Long,to:Long): List<AuraCount>
  @Query("SELECT COUNT(*) FROM AuraLog WHERE timestamp BETWEEN :from AND :to") suspend fun auraCount(from:Long,to:Long): Int
  data class AuraCount(val auraType:String, val c:Int)

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertSleep(x: SleepLog)
  @Query("SELECT * FROM SleepLog WHERE dateStart BETWEEN :from AND :to ORDER BY dateStart DESC") fun sleepBetween(from:Long,to:Long): Flow<List<SleepLog>>
  @Query("SELECT * FROM SleepLog WHERE dateStart=:dateStart LIMIT 1") suspend fun sleepByDate(dateStart:Long): SleepLog?

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertMed(x: MedItem): Long
  @Query("SELECT * FROM MedItem ORDER BY type, name") fun meds(): Flow<List<MedItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertIntake(x: IntakeLog)
  @Query("SELECT * FROM IntakeLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC") fun intakeBetween(from:Long,to:Long): Flow<List<IntakeLog>>
  @Query("SELECT type, COUNT(*) as c FROM IntakeLog WHERE timestamp BETWEEN :from AND :to GROUP BY type") suspend fun intakeTypeCounts(from:Long,to:Long): List<TypeCount>
  data class TypeCount(val type:String, val c:Int)

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertActivity(x: ActivityLog)
  @Query("SELECT * FROM ActivityLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC") fun activityBetween(from:Long,to:Long): Flow<List<ActivityLog>>
  @Query("SELECT COALESCE(SUM(durationMin),0) FROM ActivityLog WHERE timestamp BETWEEN :from AND :to") suspend fun activityDurationSum(from:Long,to:Long): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMood(x: MoodLog)
  @Query("SELECT * FROM MoodLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC") fun moodBetween(from:Long,to:Long): Flow<List<MoodLog>>
  @Query(
    "SELECT COUNT(*) FROM MoodLog WHERE timestamp BETWEEN :from AND :to AND moodMain IN (" +
      "'Sinirli','Kaygili','Uzgun','Sinirli ','Kaygili ','Uzgun ','Sinirli?','Kaygili?','Uzgun?'," +
      "'Sinirli','Kaygılı','Üzgün'" +
    ")"
  )
  suspend fun negativeMoodCount(from:Long,to:Long): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertHealth(x: HealthChangeLog)
  @Query("SELECT * FROM HealthChangeLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC") fun healthBetween(from:Long,to:Long): Flow<List<HealthChangeLog>>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSeizure(x: SeizureLog)
  @Query("SELECT * FROM SeizureLog WHERE timestampStart BETWEEN :from AND :to ORDER BY timestampStart DESC") fun seizureBetween(from:Long,to:Long): Flow<List<SeizureLog>>
  @Query("SELECT COUNT(*) FROM SeizureLog WHERE timestampStart BETWEEN :from AND :to") suspend fun seizureCount(from:Long,to:Long): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSmokeEvent(x: SmokeEventLog)
  @Query("SELECT * FROM SmokeEventLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC") fun smokeBetween(from:Long,to:Long): Flow<List<SmokeEventLog>>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertSmokeSlot(x: SmokePlanSlot)
  @Query("SELECT * FROM SmokePlanSlot WHERE ts BETWEEN :from AND :to ORDER BY ts") suspend fun smokeSlotsBetween(from:Long,to:Long): List<SmokePlanSlot>
  @Query("DELETE FROM SmokePlanSlot WHERE ts BETWEEN :from AND :to") suspend fun deleteSmokeSlotsBetween(from:Long,to:Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertPlace(x: PlaceCheckInLog)
  @Query("SELECT * FROM PlaceCheckInLog WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC") fun placesBetween(from:Long,to:Long): Flow<List<PlaceCheckInLog>>
}
