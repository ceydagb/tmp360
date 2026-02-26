package com.temporal.app.data.entities
import androidx.room.*
@Entity data class SmokeEventLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val timestamp:Long, val smoked:Boolean)
