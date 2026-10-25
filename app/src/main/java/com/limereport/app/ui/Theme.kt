package com.limereport.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LimeGreen = Color(0xFF2E7D32)
private val LimeAccent = Color(0xFFC6FF00)

@Composable
fun LimeTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = LimeAccent, onPrimary = Color(0xFF1B3A00), secondary = Color(0xFF9CD67D))
    } else {
        lightColorScheme(primary = LimeGreen, onPrimary = Color.White, secondary = Color(0xFF558B2F))
    }
    MaterialTheme(colorScheme = colors, content = content)
}
