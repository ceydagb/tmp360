package com.temporal.app.data.entities
import androidx.room.*
@Entity data class MoodLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val timestamp:Long, val moodMain:String, val moodSub:String?, val notes:String?)
