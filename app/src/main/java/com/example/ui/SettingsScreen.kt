package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.model.LanguageCatalog

@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val fromLang by preferencesManager.fromLang.collectAsState()
    val toLang by preferencesManager.toLang.collectAsState()
    val engine by preferencesManager.engine.collectAsState()
    val deeplKey by preferencesManager.deeplKey.collectAsState()
    val googleKey by preferencesManager.googleKey.collectAsState()
    val autoTranslate by preferencesManager.autoTranslate.collectAsState()
    val tone by preferencesManager.tone.collectAsState()
    val showBubble by preferencesManager.showBubble.collectAsState()
    val vibrate by preferencesManager.vibrate.collectAsState()

    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }
    var showEngineMenu by remember { mutableStateOf(false) }
    var showToneMenu by remember { mutableStateOf(false) }

    var showDeeplKeyVisible by remember { mutableStateOf(false) }
    var showGoogleKeyVisible by remember { mutableStateOf(false) }

    val fromLangObj = LanguageCatalog.findByCode(fromLang)
    val toLangObj = LanguageCatalog.findByCode(toLang)

    val canDrawOverlay = Settings.canDrawOverlays(context)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Default Languages
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🌐 Default Translation Pair",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Source Language
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Input Language (From)", style = MaterialTheme.typography.bodyMedium)
                        Text("Default language typed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .testTag("settings_from_lang_selector")
                            .clickable { showFromMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${fromLangObj.flag} ${fromLangObj.name}", fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showFromMenu,
                            onDismissRequest = { showFromMenu = false }
                        ) {
                            LanguageCatalog.languages.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.flag} ${item.name} (${item.nativeName})") },
                                    onClick = {
                                        preferencesManager.setFromLang(item.code)
                                        showFromMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Target Language
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Target Language (To)", style = MaterialTheme.typography.bodyMedium)
                        Text("Language sent to chats", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .testTag("settings_to_lang_selector")
                            .clickable { showToMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${toLangObj.flag} ${toLangObj.name}", fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showToMenu,
                            onDismissRequest = { showToMenu = false }
                        ) {
                            LanguageCatalog.languages.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.flag} ${item.name} (${item.nativeName})") },
                                    onClick = {
                                        preferencesManager.setToLang(item.code)
                                        showToMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Translation Engine & Keys
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "🚀 Translation Engine",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Engine", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = when (engine) {
                                "deepl" -> "DeepL API (Requires API Key)"
                                "google" -> "Google Cloud Translate (API Key)"
                                else -> "MyMemory Free API (No key required)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .testTag("settings_engine_selector")
                            .clickable { showEngineMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (engine) {
                                    "deepl" -> "DeepL"
                                    "google" -> "Google"
                                    else -> "MyMemory (Free)"
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        DropdownMenu(
                            expanded = showEngineMenu,
                            onDismissRequest = { showEngineMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("MyMemory Free API (Recommended, No Key)") },
                                onClick = {
                                    preferencesManager.setEngine("mymemory")
                                    showEngineMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("DeepL API") },
                                onClick = {
                                    preferencesManager.setEngine("deepl")
                                    showEngineMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Google Cloud Translate API") },
                                onClick = {
                                    preferencesManager.setEngine("google")
                                    showEngineMenu = false
                                }
                            )
                        }
                    }
                }

                // If DeepL selected, show key input
                if (engine == "deepl") {
                    OutlinedTextField(
                        value = deeplKey,
                        onValueChange = { preferencesManager.setDeeplKey(it) },
                        label = { Text("DeepL API Key") },
                        placeholder = { Text("e.g. 12345678-abcd-...:fx") },
                        visualTransformation = if (showDeeplKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showDeeplKeyVisible = !showDeeplKeyVisible }) {
                                Icon(
                                    imageVector = if (showDeeplKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("deepl_key_input")
                    )
                }

                // If Google selected, show key input
                if (engine == "google") {
                    OutlinedTextField(
                        value = googleKey,
                        onValueChange = { preferencesManager.setGoogleKey(it) },
                        label = { Text("Google Cloud Translation API Key") },
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = if (showGoogleKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showGoogleKeyVisible = !showGoogleKeyVisible }) {
                                Icon(
                                    imageVector = if (showGoogleKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("google_key_input")
                    )
                }

                // Tone selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Translation Tone", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = tone.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { showToneMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tone.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showToneMenu,
                            onDismissRequest = { showToneMenu = false }
                        ) {
                            listOf("default", "formal", "informal").forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        preferencesManager.setTone(t)
                                        showToneMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Typing & Automation Preferences
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "⚡ Typing & Automation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Auto-translate toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Translate Sentences", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Automatically translates sentence upon typing punctuation (. ? ! ।)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoTranslate,
                        onCheckedChange = { preferencesManager.setAutoTranslate(it) },
                        modifier = Modifier.testTag("auto_translate_switch")
                    )
                }

                // Vibration toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Haptic Feedback", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Vibrate on key presses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { preferencesManager.setVibrate(it) },
                        modifier = Modifier.testTag("vibrate_switch")
                    )
                }
            }
        }

        // Section: Incoming Messages Bubble & Permissions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "💬 Incoming Message Bubble",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Floating Bangla Bubble", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Displays incoming WhatsApp & Telegram messages translated into Bangla", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showBubble,
                        onCheckedChange = { preferencesManager.setShowBubble(it) },
                        modifier = Modifier.testTag("show_bubble_switch")
                    )
                }

                // Overlay Permission Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Draw Over Other Apps", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (canDrawOverlay) "Granted ✓" else "Required for floating bubble",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (canDrawOverlay) Color(0xFF00C853) else MaterialTheme.colorScheme.error
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("overlay_permission_button")
                    ) {
                        Text(if (canDrawOverlay) "Granted" else "Grant")
                    }
                }

                // Accessibility Service Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accessibility Service", style = MaterialTheme.typography.bodyMedium)
                        Text("Required to read incoming text", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("accessibility_permission_button")
                    ) {
                        Text("Configure")
                    }
                }
            }
        }
    }
}
