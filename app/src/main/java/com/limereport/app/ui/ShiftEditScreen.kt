package com.limereport.app.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.limereport.app.VoiceParser
import com.limereport.app.data.Rates
import com.limereport.app.data.Shift
import com.limereport.app.data.total
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftEditScreen(
    existing: Shift?,
    rates: Rates,
    onSave: (Shift) -> Unit,
    onDelete: (Shift) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    // Existing shifts keep the rates they were logged with; new shifts use today's rates.
    val r = existing?.let {
        Rates(it.relocateScooterRate, it.relocateBikeRate, it.deployScooterRate, it.deployBikeRate, it.damagedRate, it.batterySwapRate)
    } ?: rates

    var start by rememberSaveable { mutableStateOf(existing?.startMillis ?: defaultStart()) }
    var end by rememberSaveable { mutableStateOf(existing?.endMillis ?: defaultEnd()) }
    val relocS = rememberSaveable { mutableStateOf(existing?.relocateScooters?.toString() ?: "") }
    val relocB = rememberSaveable { mutableStateOf(existing?.relocateBikes?.toString() ?: "") }
    val depS = rememberSaveable { mutableStateOf(existing?.deployScooters?.toString() ?: "") }
    val depB = rememberSaveable { mutableStateOf(existing?.deployBikes?.toString() ?: "") }
    val dmg = rememberSaveable { mutableStateOf(existing?.damagedVehicles?.toString() ?: "") }
    val bat = rememberSaveable { mutableStateOf(existing?.batterySwaps?.toString() ?: "") }
    var note by rememberSaveable { mutableStateOf(existing?.note ?: "") }
    var heard by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    fun n(s: MutableState<String>) = s.value.toIntOrNull() ?: 0
    val total = n(relocS) * r.relocateScooter + n(relocB) * r.relocateBike +
        n(depS) * r.deployScooter + n(depB) * r.deployBike + n(dmg) * r.damaged +
        n(bat) * r.batterySwap
    val timesValid = end > start

    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            ?: return@rememberLauncherForActivityResult
        heard = text
        val parsed = VoiceParser.parse(text)
        parsed.relocateScooters?.let { relocS.value = it.toString() }
        parsed.relocateBikes?.let { relocB.value = it.toString() }
        parsed.deployScooters?.let { depS.value = it.toString() }
        parsed.deployBikes?.let { depB.value = it.toString() }
        parsed.damaged?.let { dmg.value = it.toString() }
        parsed.batterySwaps?.let { bat.value = it.toString() }
        if (parsed.isEmpty) {
            Toast.makeText(context, "Couldn't find any counts. Try: “relocated 12 scooters, 4 bikes”", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Log shift" else "Edit shift") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete shift") }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("Shift time")
            DateTimeRow("Start", start) { start = it }
            DateTimeRow("End", end) { end = it }
            if (!timesValid) {
                Text("End must be after start.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            FilledTonalButton(
                onClick = { startSpeech(context) { speech.launch(it) } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Text("  Say your counts")
            }
            Text(
                heard?.let { "Heard: “$it”" }
                    ?: "e.g. “relocated 12 scooters and 5 bikes, deployed 20 scooters and 3 bikes, 4 damaged, 15 batteries”",
                style = MaterialTheme.typography.bodySmall,
            )

            SectionTitle("Relocate (remote spot → hub)")
            CountRow("Scooters", r.relocateScooter, relocS)
            CountRow("Bikes", r.relocateBike, relocB)
            SectionTitle("Deploy (warehouse → hub)")
            CountRow("Scooters", r.deployScooter, depS)
            CountRow("Bikes", r.deployBike, depB)
            SectionTitle("Damaged (city → warehouse)")
            CountRow("Vehicles", r.damaged, dmg)
            SectionTitle("Battery swaps")
            CountRow("Batteries", r.batterySwap, bat)

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Shift total", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Text(shekels(total), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = {
                    onSave(
                        Shift(
                            id = existing?.id ?: 0,
                            startMillis = start,
                            endMillis = end,
                            relocateScooters = n(relocS),
                            relocateBikes = n(relocB),
                            deployScooters = n(depS),
                            deployBikes = n(depB),
                            damagedVehicles = n(dmg),
                            relocateScooterRate = r.relocateScooter,
                            relocateBikeRate = r.relocateBike,
                            deployScooterRate = r.deployScooter,
                            deployBikeRate = r.deployBike,
                            damagedRate = r.damaged,
                            batterySwaps = n(bat),
                            batterySwapRate = r.batterySwap,
                            note = note.trim(),
                        ),
                    )
                },
                enabled = timesValid,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (existing == null) "Save shift" else "Save changes") }
        }
    }

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this shift?") },
            text = { Text(formatShiftRange(existing.startMillis, existing.endMillis) + "\n" + shekels(existing.total)) },
            confirmButton = { TextButton(onClick = { onDelete(existing) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

private fun startSpeech(context: Context, launch: (Intent) -> Unit) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        // Counts are parsed from English words, so ask for English regardless of phone language.
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your counts, e.g. “relocated 12 scooters, 5 bikes”")
    }
    try {
        launch(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Voice input isn't available on this phone", Toast.LENGTH_LONG).show()
    }
}

/** New shift defaults: ends now, started 12 hours earlier (both rounded to 15 minutes). */
private fun defaultEnd(): Long = roundTo15(LocalDateTime.now())
private fun defaultStart(): Long = roundTo15(LocalDateTime.now().minusHours(12))

private fun roundTo15(t: LocalDateTime): Long {
    val rounded = t.truncatedTo(ChronoUnit.HOURS).plusMinutes(((t.minute + 7) / 15 * 15).toLong())
    return rounded.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun DateTimeRow(label: String, millis: Long, onChange: (Long) -> Unit) {
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val dt = Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()
    fun emit(newDt: LocalDateTime) = onChange(newDt.atZone(zone).toInstant().toEpochMilli())

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, Modifier.width(48.dp))
        OutlinedButton(
            onClick = {
                DatePickerDialog(context, { _, y, m, d -> emit(dt.withYear(y).withMonth(m + 1).withDayOfMonth(d)) },
                    dt.year, dt.monthValue - 1, dt.dayOfMonth).show()
            },
            modifier = Modifier.weight(1f),
        ) { Text(formatDay(millis)) }
        OutlinedButton(
            onClick = {
                TimePickerDialog(context, { _, h, min -> emit(dt.withHour(h).withMinute(min)) }, dt.hour, dt.minute, true).show()
            },
        ) { Text(formatTime(millis)) }
    }
}

@Composable
private fun CountRow(label: String, rate: Int, state: MutableState<String>) {
    val count = state.value.toIntOrNull() ?: 0
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text("${shekels(rate)} each · ${shekels(count * rate)}", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = { state.value = (count - 1).coerceAtLeast(0).toString() }) {
            Icon(Icons.Default.Remove, contentDescription = "Less $label")
        }
        OutlinedTextField(
            value = state.value,
            onValueChange = { v -> state.value = v.filter { it.isDigit() }.take(4) },
            placeholder = { Text("0", Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(80.dp),
        )
        IconButton(onClick = { state.value = (count + 1).toString() }) {
            Icon(Icons.Default.Add, contentDescription = "More $label")
        }
    }
}
