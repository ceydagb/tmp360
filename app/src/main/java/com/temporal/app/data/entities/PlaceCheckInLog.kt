package com.temporal.app.data.entities
import androidx.room.*
@Entity
data class PlaceCheckInLog(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long,
  val placeText: String?,
  val lat: Double?,
  val lon: Double?
)
