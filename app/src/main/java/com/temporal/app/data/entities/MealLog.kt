package com.temporal.app.data.entities
import androidx.room.*
@Entity data class MealLog(@PrimaryKey(autoGenerate=true) val id:Long=0, val timestamp:Long, val foodId:Long?, val foodName:String, val portionText:String, val kcal:Int, val carbsG:Int, val proteinG:Int, val fatG:Int)
