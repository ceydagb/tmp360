package com.temporal.app.data.entities
import androidx.room.*
@Entity data class AuraLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val timestamp:Long, val auraType:String, val intensity:Int, val notes:String?)
