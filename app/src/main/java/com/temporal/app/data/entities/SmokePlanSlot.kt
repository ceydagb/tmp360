package com.temporal.app.data.entities
import androidx.room.*
@Entity(indices=[Index(value=["ts"], unique=true)])
data class SmokePlanSlot(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val ts: Long
)
