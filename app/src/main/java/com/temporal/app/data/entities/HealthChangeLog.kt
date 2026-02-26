package com.temporal.app.data.entities
import androidx.room.*
@Entity data class HealthChangeLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val timestamp:Long, val category:String, val severity:Int, val notes:String?)
