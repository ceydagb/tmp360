package com.temporal.app.data.seed

import android.content.Context
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.FoodItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SeedData {
  @Volatile private var done = false

  fun ensure(context: Context) {
    if (done) return
    done = true
    CoroutineScope(Dispatchers.IO).launch {
      val dao = TemporalDb.get(context).dao()
      val foods = listOf(
        FoodItem(category="Kahvaltı", name="Yumurta (1 adet)", kcal=78, carbsG=1, proteinG=6, fatG=5),
        FoodItem(category="Kahvaltı", name="Avokado (100g)", kcal=160, carbsG=9, proteinG=2, fatG=15),
        FoodItem(category="Protein", name="Somon (100g)", kcal=208, carbsG=0, proteinG=20, fatG=13),
        FoodItem(category="Protein", name="Tavuk göğüs (100g)", kcal=165, carbsG=0, proteinG=31, fatG=4),
        FoodItem(category="Yağ", name="Zeytinyağı (1 yk)", kcal=119, carbsG=0, proteinG=0, fatG=14),
        FoodItem(category="Kuruyemiş", name="Ceviz (30g)", kcal=196, carbsG=4, proteinG=5, fatG=20),
        FoodItem(category="Süt/İçecek", name="Ayran (200ml)", kcal=80, carbsG=6, proteinG=4, fatG=4),
        FoodItem(category="Süt/İçecek", name="Kefir (200ml)", kcal=120, carbsG=8, proteinG=6, fatG=6)
      )
      foods.forEach { dao.upsertFood(it) }
    }
  }
}
