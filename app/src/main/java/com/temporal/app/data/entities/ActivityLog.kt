package com.temporal.app.data.entities
import androidx.room.*
@Entity data class ActivityLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val timestamp:Long, val category:String, val durationMin:Int, val notes:String?)
