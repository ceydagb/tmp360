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
import com.temporal.app.data.entities.MealLog
import com.temporal.app.ui.components.TemporalTopBar
import com.temporal.app.ui.components.showDatePicker
import com.temporal.app.ui.components.showTimePicker
import com.temporal.app.util.TimeFmt
import kotlinx.coroutines.launch

@Composable
fun AddMealScreen(nav: NavController) {
  val ctx = LocalContext.current
  val scope = rememberCoroutineScope()
  val dao = remember { TemporalDb.get(ctx).dao() }
  val foods by dao.foods().collectAsState(initial = emptyList())

  var ts by remember { mutableStateOf(System.currentTimeMillis()) }
  var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
  var manualName by remember { mutableStateOf("") }
  var portion by remember { mutableStateOf("1 porsiyon") }

  var exp by remember { mutableStateOf(false) }
  val categories = foods.map { it.category }.distinct()
  var cat by remember { mutableStateOf(categories.firstOrNull() ?: "Kahvaltı") }

  val foodsInCat = foods.filter { it.category == cat }

  Scaffold(topBar = {
    TemporalTopBar("Öğün Ekle", onBack = { nav.popBackStack() }, actions = {
      TextButton(onClick = { nav.navigate(Routes.AddFood) }) { Text("Yemek ekle") }
    })
  }) { pad ->
    Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

      Text("Zaman: ${TimeFmt.format(ts)}")
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { showDatePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Tarih") }
        Button(onClick = { showTimePicker(ctx, ts) { ts = it } }, modifier = Modifier.weight(1f)) { Text("Saat") }
      }

      Text("Kategori")
      var catExpanded by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = !catExpanded }) {
        OutlinedTextField(value = cat, onValueChange = {}, readOnly = true, label = { Text("Kategori") }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
          categories.forEach {
            DropdownMenuItem(text = { Text(it) }, onClick = { cat = it; selectedFood = null; catExpanded = false })
          }
        }
      }

      Text("Hazır listeden seç")
      var expanded by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
          value = selectedFood?.name ?: "",
          onValueChange = {},
          readOnly = true,
          label = { Text("Yemek") },
          modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          foodsInCat.forEach { f ->
            DropdownMenuItem(
              text = { Text("${f.name} • ${f.kcal}kcal • K${f.carbsG} P${f.proteinG} Y${f.fatG}") },
              onClick = { selectedFood = f; manualName = ""; expanded = false }
            )
          }
        }
      }

      OutlinedTextField(manualName, { manualName = it; if (it.isNotBlank()) selectedFood = null },
        label = { Text("Bulamadın mı? Manuel yaz") }, modifier = Modifier.fillMaxWidth())

      OutlinedTextField(portion, { portion = it }, label = { Text("Porsiyon") }, modifier = Modifier.fillMaxWidth())

      Button(onClick = {
        val f = selectedFood
        val name = if (f != null) f.name else manualName.trim()
        if (name.isBlank()) return@Button
        scope.launch {
          val kcal = f?.kcal ?: 0
          val c = f?.carbsG ?: 0
          val p = f?.proteinG ?: 0
          val fat = f?.fatG ?: 0
          dao.insertMeal(MealLog(timestamp = ts, foodId = f?.id, foodName = name, portionText = portion, kcal = kcal, carbsG = c, proteinG = p, fatG = fat))
          nav.popBackStack()
        }
      }, modifier = Modifier.fillMaxWidth()) { Text("Kaydet") }
    }
  }
}
