package com.temporal.app.data.entities
import androidx.room.*
@Entity data class MedItem(@PrimaryKey(autoGenerate=true) val id:Long=0, val type:String, val name:String, val mgPerDose:Int)
