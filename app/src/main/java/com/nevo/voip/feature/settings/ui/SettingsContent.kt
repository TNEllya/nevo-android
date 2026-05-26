package com.nevo.voip.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nevo.voip.core.datastore.NevoPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { NevoPreferences(context) }
    val scope = rememberCoroutineScope()

    var themeMode by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var pttEnabled by remember { mutableStateOf(false) }
    var vadSensitivity by remember { mutableFloatStateOf(0f) }
    var noiseSuppression by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        themeMode = preferences.themeMode.first()
        language = preferences.language.first()
        pttEnabled = preferences.pttEnabled.first()
        vadSensitivity = preferences.vadSensitivity.first().toFloat()
        noiseSuppression = preferences.noiseSuppressionLevel.first().toFloat()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader("Appearance")
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
            )
            ThemeOption("Light", themeMode == NevoPreferences.THEME_LIGHT) {
                themeMode = NevoPreferences.THEME_LIGHT
                scope.launch { preferences.setThemeMode(NevoPreferences.THEME_LIGHT) }
            }
            ThemeOption("Dark", themeMode == NevoPreferences.THEME_DARK) {
                themeMode = NevoPreferences.THEME_DARK
                scope.launch { preferences.setThemeMode(NevoPreferences.THEME_DARK) }
            }
            ThemeOption("System default", themeMode == NevoPreferences.THEME_SYSTEM) {
                themeMode = NevoPreferences.THEME_SYSTEM
                scope.launch { preferences.setThemeMode(NevoPreferences.THEME_SYSTEM) }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()

            SettingsSectionHeader("Language")
            LanguageOption("English", language == NevoPreferences.LANGUAGE_EN) {
                language = NevoPreferences.LANGUAGE_EN
                scope.launch { preferences.setLanguage(NevoPreferences.LANGUAGE_EN) }
            }
            LanguageOption("简体中文", language == NevoPreferences.LANGUAGE_ZH_CN) {
                language = NevoPreferences.LANGUAGE_ZH_CN
                scope.launch { preferences.setLanguage(NevoPreferences.LANGUAGE_ZH_CN) }
            }
            LanguageOption("繁體中文", language == NevoPreferences.LANGUAGE_ZH_TW) {
                language = NevoPreferences.LANGUAGE_ZH_TW
                scope.launch { preferences.setLanguage(NevoPreferences.LANGUAGE_ZH_TW) }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()

            SettingsSectionHeader("Audio")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Push to Talk",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = pttEnabled,
                    onCheckedChange = {
                        pttEnabled = it
                        scope.launch { preferences.setPttEnabled(it) }
                    }
                )
            }

            Text(
                text = "VAD Sensitivity: ${vadSensitivity.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            Slider(
                value = vadSensitivity,
                onValueChange = { vadSensitivity = it },
                onValueChangeFinished = {
                    scope.launch {
                        preferences.setVadSensitivity(vadSensitivity.toInt())
                    }
                },
                valueRange = 0f..3f,
                steps = 2,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = "Noise Suppression: ${noiseSuppression.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            Slider(
                value = noiseSuppression,
                onValueChange = { noiseSuppression = it },
                onValueChangeFinished = {
                    scope.launch {
                        preferences.setNoiseSuppressionLevel(noiseSuppression.toInt())
                    }
                },
                valueRange = 0f..3f,
                steps = 2,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()

            SettingsSectionHeader("About")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "NEVO VoIP",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
            Text(
                text = "Build: 1",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 16.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.padding(start = 8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.padding(start = 8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}