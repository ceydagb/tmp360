package com.temporal.app.data.entities
import androidx.room.*
@Entity data class WaterLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val timestamp:Long, val amountMl:Int)
