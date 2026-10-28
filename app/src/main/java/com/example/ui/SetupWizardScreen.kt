package com.example.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TranslationRepository
import com.example.service.FloatingBubbleManager
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "Just now"
)

@Composable
fun SetupWizardScreen(
    translationRepository: TranslationRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imm = remember { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    val scope = rememberCoroutineScope()
    val bubbleManager = remember { FloatingBubbleManager(context) }

    var isImeEnabled by remember { mutableStateOf(false) }
    var isImeSelected by remember { mutableStateOf(false) }

    fun checkImeStatus() {
        val enabledMethods = imm.enabledInputMethodList
        val myPackage = context.packageName
        isImeEnabled = enabledMethods.any { it.packageName == myPackage }

        val currentIme = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: ""
        isImeSelected = currentIme.contains(myPackage)
    }

    LaunchedEffect(Unit) {
        checkImeStatus()
    }

    // Playground chat messages
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("Welcome to AutoLingo! Type in Bangla below and click [Translate & Send] or test incoming messages.", isUser = false),
            ChatMessage("উদাহরণ: 'আপনি কেমন আছেন?' বা 'আমি ভালো আছি'", isUser = false)
        )
    }
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AutoLingo Keyboard Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Translate Bangla to English & 12+ languages seamlessly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Setup Steps
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
                    text = "Quick Activation Steps",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Step 1: Enable Keyboard in System Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepIndicator(number = 1, isCompleted = isImeEnabled)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "1. Enable in System",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isImeEnabled) "Active in Android settings" else "Turn on AutoLingo toggle",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isImeEnabled) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = if (isImeEnabled) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors(),
                        modifier = Modifier.testTag("step1_enable_keyboard_button")
                    ) {
                        Text(if (isImeEnabled) "Enabled ✓" else "Enable")
                    }
                }

                // Step 2: Select Keyboard as Default
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepIndicator(number = 2, isCompleted = isImeSelected)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "2. Select Keyboard",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isImeSelected) "Currently active keyboard" else "Switch default keyboard",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isImeSelected) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            imm.showInputMethodPicker()
                        },
                        colors = if (isImeSelected) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors(),
                        modifier = Modifier.testTag("step2_switch_keyboard_button")
                    ) {
                        Text(if (isImeSelected) "Selected ✓" else "Select")
                    }
                }
            }
        }

        // Live Interactive Playground
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💬 Live Testing Playground",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = {
                            // Simulate incoming WhatsApp message
                            scope.launch {
                                val incoming = "Hello! Where are you right now? Let's meet at 5 PM."
                                val result = translationRepository.translate(
                                    text = incoming,
                                    fromLang = "en",
                                    toLang = "bn",
                                    isIncoming = true
                                )
                                val translated = result.getOrNull() ?: "হ্যালো! আপনি এখন কোথায় আছেন? আসুন বিকেল ৫টায় দেখা করি।"
                                bubbleManager.showBubble(incoming, translated)
                                chatMessages.add(ChatMessage("Incoming (EN): $incoming\n➜ Translated: $translated", isUser = false))
                            }
                        },
                        modifier = Modifier.testTag("simulate_incoming_bubble_button")
                    ) {
                        Text("Simulate Bubble", fontSize = 11.sp)
                    }
                }

                Text(
                    text = "Tap the input field to test typing in Bangla with AutoLingo keyboard, or click Send to see outgoing translation:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Mini Chat Stream
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chatMessages.takeLast(6).forEach { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Input Box for testing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("টাইপ করুন বাংলায়...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("playground_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val currentText = inputText
                                inputText = ""
                                scope.launch {
                                    val res = translationRepository.translate(
                                        text = currentText,
                                        fromLang = "bn",
                                        toLang = "en"
                                    )
                                    val translated = res.getOrNull() ?: currentText
                                    chatMessages.add(ChatMessage("Bangla: $currentText\n➜ English: $translated", isUser = true))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp).testTag("playground_send_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(number: Int, isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                if (isCompleted) Color(0xFF00C853) else MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Done",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = "$number",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
