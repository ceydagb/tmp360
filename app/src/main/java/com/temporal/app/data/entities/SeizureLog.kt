package com.temporal.app.data.entities
import androidx.room.*
@Entity data class SeizureLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val timestampStart:Long, val durationSec:Int, val consciousnessLoss:Boolean, val postictalMin:Int, val place:String?, val context:String?, val triggers:String?, val symptoms:String, val notes:String?)
