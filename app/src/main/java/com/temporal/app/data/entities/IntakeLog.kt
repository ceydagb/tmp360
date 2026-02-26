package com.temporal.app.data.entities
import androidx.room.*
@Entity data class IntakeLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val timestamp:Long, val itemId:Long, val type:String, val mg:Int, val count:Int)
