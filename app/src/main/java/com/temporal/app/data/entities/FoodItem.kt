package com.temporal.app.data.entities
import androidx.room.*
@Entity(indices=[Index(value=["name"], unique=true)]) data class FoodItem(@PrimaryKey(autoGenerate=true) val id:Long=0, val category:String, val name:String, val kcal:Int, val carbsG:Int, val proteinG:Int, val fatG:Int)
