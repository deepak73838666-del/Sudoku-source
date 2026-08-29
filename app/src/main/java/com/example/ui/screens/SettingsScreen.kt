package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val mistakesOn by viewModel.mistakesOn.collectAsState()
    val showTimer by viewModel.showTimer.collectAsState()
    val highlightSame by viewModel.highlightSame.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                SettingsCategory("Appearance")
                SettingsSwitch(
                    title = "Dark Mode",
                    subtitle = "Use dark color scheme",
                    checked = themeMode == "Dark",
                    onCheckedChange = { viewModel.setThemeMode(if (it) "Dark" else "Light") }
                )
            }
            
            item {
                SettingsCategory("Gameplay")
                SettingsSwitch(
                    title = "Show Mistakes",
                    subtitle = "Highlight incorrect numbers",
                    checked = mistakesOn,
                    onCheckedChange = { viewModel.setMistakesOn(it) }
                )
                SettingsSwitch(
                    title = "Show Timer",
                    subtitle = "Display timer during puzzle",
                    checked = showTimer,
                    onCheckedChange = { viewModel.setShowTimer(it) }
                )
                SettingsSwitch(
                    title = "Highlight Same Numbers",
                    subtitle = "Highlight all instances of selected number",
                    checked = highlightSame,
                    onCheckedChange = { viewModel.setHighlightSame(it) }
                )
            }
            
            item {
                SettingsCategory("About")
                SettingsItem("Privacy Policy")
                SettingsItem("Rate the App")
                SettingsItem("Share the App")
                SettingsItem("Version 1.0.0")
            }
        }
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}
