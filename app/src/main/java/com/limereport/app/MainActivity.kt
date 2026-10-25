package com.limereport.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limereport.app.ui.LimeTheme
import com.limereport.app.ui.SettingsScreen
import com.limereport.app.ui.ShiftEditScreen
import com.limereport.app.ui.WeekScreen

/** Screens: the week list, the shift editor (id 0 = new shift) and rate settings. */
private const val SCREEN_WEEK = "week"
private const val SCREEN_EDIT = "edit"
private const val SCREEN_SETTINGS = "settings"

class MainActivity : ComponentActivity() {
    private val viewModel: ShiftViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LimeTheme {
                var screen by rememberSaveable { mutableStateOf(SCREEN_WEEK) }
                var editId by rememberSaveable { mutableStateOf(0L) }
                val shifts by viewModel.shifts.collectAsStateWithLifecycle()
                val rates by viewModel.rates.collectAsStateWithLifecycle()

                BackHandler(enabled = screen != SCREEN_WEEK) { screen = SCREEN_WEEK }

                when (screen) {
                    SCREEN_EDIT -> ShiftEditScreen(
                        existing = shifts.firstOrNull { it.id == editId },
                        rates = rates,
                        onSave = { viewModel.save(it); screen = SCREEN_WEEK },
                        onDelete = { viewModel.delete(it); screen = SCREEN_WEEK },
                        onBack = { screen = SCREEN_WEEK },
                    )
                    SCREEN_SETTINGS -> SettingsScreen(
                        rates = rates,
                        onSave = { viewModel.saveRates(it); screen = SCREEN_WEEK },
                        onBack = { screen = SCREEN_WEEK },
                    )
                    else -> WeekScreen(
                        shifts = shifts,
                        onAdd = { editId = 0L; screen = SCREEN_EDIT },
                        onOpen = { editId = it.id; screen = SCREEN_EDIT },
                        onSettings = { screen = SCREEN_SETTINGS },
                    )
                }
            }
        }
    }
}
