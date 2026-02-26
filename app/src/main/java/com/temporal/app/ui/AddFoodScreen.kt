package com.temporal.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.temporal.app.data.TemporalDb
import com.temporal.app.data.entities.FoodItem
import com.temporal.app.ui.components.TemporalTopBar
import kotlinx.coroutines.launch

@Composable
fun AddFoodScreen(nav: NavController) {
  val ctx = LocalContext.current
  val dao = remember { TemporalDb.get(ctx).dao() }
  val scope = rememberCoroutineScope()

  var category by remember { mutableStateOf("Kahvaltı") }
  var name by remember { mutableStateOf("") }
  var kcal by remember { mutableStateOf("") }
  var carb by remember { mutableStateOf("") }
  var protein by remember { mutableStateOf("") }
  var fat by remember { mutableStateOf("") }

  Scaffold(topBar = { TemporalTopBar("Yemek Tanımı", onBack = { nav.popBackStack() }) }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      OutlinedTextField(category, { category = it }, label = { Text("Kategori") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(name, { name = it }, label = { Text("Yemek adı") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(kcal, { kcal = it.filter(Char::isDigit) }, label = { Text("Kalori") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(carb, { carb = it.filter(Char::isDigit) }, label = { Text("Karb (g)") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(protein, { protein = it.filter(Char::isDigit) }, label = { Text("Protein (g)") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(fat, { fat = it.filter(Char::isDigit) }, label = { Text("Yağ (g)") }, modifier = Modifier.fillMaxWidth())
      Button(onClick = {
        val n = name.trim()
        if (n.isBlank()) return@Button
        scope.launch {
          dao.upsertFood(FoodItem(category = category.trim().ifBlank { "Genel" }, name = n,
            kcal = kcal.toIntOrNull() ?: 0, carbsG = carb.toIntOrNull() ?: 0,
            proteinG = protein.toIntOrNull() ?: 0, fatG = fat.toIntOrNull() ?: 0))
          nav.popBackStack()
        }
      }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet") }
    }
  }
}
