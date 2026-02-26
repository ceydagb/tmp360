package com.temporal.app.data.entities
import androidx.room.*
@Entity(indices=[Index(value=["dateStart"], unique=true)]) data class SleepLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val dateStart:Long, val sleepStart:Long, val wake:Long, val quality:Int, val notes:String?)
