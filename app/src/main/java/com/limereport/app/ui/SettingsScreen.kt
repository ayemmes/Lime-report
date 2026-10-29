package com.limereport.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.limereport.app.data.Rates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(rates: Rates, onSave: (Rates) -> Unit, onBack: () -> Unit) {
    val relocS = remember { mutableStateOf(rates.relocateScooter.toString()) }
    val relocB = remember { mutableStateOf(rates.relocateBike.toString()) }
    val depS = remember { mutableStateOf(rates.deployScooter.toString()) }
    val depB = remember { mutableStateOf(rates.deployBike.toString()) }
    val dmg = remember { mutableStateOf(rates.damaged.toString()) }
    val bat = remember { mutableStateOf(rates.batterySwap.toString()) }
    val all = listOf(relocS, relocB, depS, depB, dmg, bat)
    val valid = all.all { it.value.toIntOrNull()?.let { v -> v >= 0 } == true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rates (₪ per vehicle)") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "New shifts use these rates. Shifts you already logged keep the rates they were saved with.",
                style = MaterialTheme.typography.bodyMedium,
            )
            RateField("Relocate scooter (remote → hub)", relocS)
            RateField("Relocate bike (remote → hub)", relocB)
            RateField("Deploy scooter (warehouse → hub)", depS)
            RateField("Deploy bike (warehouse → hub)", depB)
            RateField("Damaged vehicle (→ warehouse)", dmg)
            RateField("Battery swap (per battery)", bat)
            Button(
                onClick = {
                    onSave(
                        Rates(
                            relocateScooter = relocS.value.toInt(),
                            relocateBike = relocB.value.toInt(),
                            deployScooter = depS.value.toInt(),
                            deployBike = depB.value.toInt(),
                            damaged = dmg.value.toInt(),
                            batterySwap = bat.value.toInt(),
                        ),
                    )
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save rates") }
            TextButton(
                onClick = {
                    val d = Rates()
                    relocS.value = d.relocateScooter.toString()
                    relocB.value = d.relocateBike.toString()
                    depS.value = d.deployScooter.toString()
                    depB.value = d.deployBike.toString()
                    dmg.value = d.damaged.toString()
                    bat.value = d.batterySwap.toString()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Reset to defaults") }
        }
    }
}

@Composable
private fun RateField(label: String, state: MutableState<String>) {
    OutlinedTextField(
        value = state.value,
        onValueChange = { v -> state.value = v.filter { it.isDigit() }.take(4) },
        label = { Text(label) },
        prefix = { Text("₪") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}
