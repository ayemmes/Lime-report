package com.limereport.app.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.limereport.app.Weeks
import com.limereport.app.data.Shift
import com.limereport.app.data.total
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekScreen(
    shifts: List<Shift>,
    onAdd: () -> Unit,
    onOpen: (Shift) -> Unit,
    onSettings: () -> Unit,
) {
    val thisWeek = Weeks.weekStartOf(LocalDate.now())
    var weekEpochDay by rememberSaveable { mutableStateOf(thisWeek.toEpochDay()) }
    val week = LocalDate.ofEpochDay(weekEpochDay)
    val weekShifts = shifts.filter { Weeks.weekStartOf(it.startMillis) == week }.sortedByDescending { it.startMillis }
    val summary = WeekSummary(weekShifts)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lime Report") },
                actions = {
                    IconButton(
                        onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, weekReport(week, weekShifts, summary))
                            }
                            context.startActivity(Intent.createChooser(send, "Share week report"))
                        },
                        enabled = weekShifts.isNotEmpty(),
                    ) { Icon(Icons.Default.Share, contentDescription = "Share week report") }
                    IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = "Rates") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log shift") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { weekEpochDay -= 7 }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous week")
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(formatWeek(week), style = MaterialTheme.typography.titleMedium)
                        if (week != thisWeek) {
                            TextButton(onClick = { weekEpochDay = thisWeek.toEpochDay() }) { Text("Back to this week") }
                        } else {
                            Text("This week", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    IconButton(onClick = { weekEpochDay += 7 }, enabled = week < thisWeek) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next week")
                    }
                }
            }
            item { WeekTotalCard(summary) }
            if (weekShifts.isEmpty()) {
                item {
                    Text(
                        "No shifts logged this week.\nTap “Log shift” at the end of your shift.",
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            items(weekShifts, key = { it.id }) { shift -> ShiftCard(shift) { onOpen(shift) } }
        }
    }
}

private class WeekSummary(shifts: List<Shift>) {
    val total = shifts.sumOf { it.total }
    val shiftCount = shifts.size
    val relocateScooters = shifts.sumOf { it.relocateScooters }
    val relocateBikes = shifts.sumOf { it.relocateBikes }
    val deployScooters = shifts.sumOf { it.deployScooters }
    val deployBikes = shifts.sumOf { it.deployBikes }
    val damaged = shifts.sumOf { it.damagedVehicles }
    val batterySwaps = shifts.sumOf { it.batterySwaps }
}

@Composable
private fun WeekTotalCard(s: WeekSummary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Week total", style = MaterialTheme.typography.labelLarge)
            Text(shekels(s.total), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("${s.shiftCount} shift${if (s.shiftCount == 1) "" else "s"}", style = MaterialTheme.typography.bodyMedium)
            if (s.shiftCount > 0) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                SummaryLine("Relocated", "${s.relocateScooters} scooters · ${s.relocateBikes} bikes")
                SummaryLine("Deployed", "${s.deployScooters} scooters · ${s.deployBikes} bikes")
                SummaryLine("Damaged collected", "${s.damaged}")
                SummaryLine("Batteries swapped", "${s.batterySwaps}")
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ShiftCard(shift: Shift, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(formatDay(shift.startMillis) + " → " + formatDay(shift.endMillis), style = MaterialTheme.typography.titleSmall)
                Text("${formatTime(shift.startMillis)} – ${formatTime(shift.endMillis)}", style = MaterialTheme.typography.bodySmall)
                Text(shiftBreakdown(shift), style = MaterialTheme.typography.bodySmall)
            }
            Text(shekels(shift.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun shiftBreakdown(s: Shift): String = buildList {
    if (s.relocateScooters + s.relocateBikes > 0) add("Reloc ${s.relocateScooters}S/${s.relocateBikes}B")
    if (s.deployScooters + s.deployBikes > 0) add("Deploy ${s.deployScooters}S/${s.deployBikes}B")
    if (s.damagedVehicles > 0) add("Damaged ${s.damagedVehicles}")
    if (s.batterySwaps > 0) add("Batteries ${s.batterySwaps}")
}.ifEmpty { listOf("No tasks") }.joinToString(" · ")

private fun weekReport(week: LocalDate, shifts: List<Shift>, s: WeekSummary): String = buildString {
    appendLine("Lime week ${formatWeekShort(week)}")
    shifts.sortedBy { it.startMillis }.forEach {
        appendLine("${formatShiftRange(it.startMillis, it.endMillis)}: ${shekels(it.total)}")
    }
    appendLine("Relocated: ${s.relocateScooters} scooters, ${s.relocateBikes} bikes")
    appendLine("Deployed: ${s.deployScooters} scooters, ${s.deployBikes} bikes")
    appendLine("Damaged collected: ${s.damaged}")
    appendLine("Batteries swapped: ${s.batterySwaps}")
    append("Total: ${shekels(s.total)}")
}
