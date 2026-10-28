package com.example.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.LanguageCatalog
import com.example.model.SupportedLanguage

@Composable
fun AutoLingoKeyboardView(
    fromLangCode: String,
    toLangCode: String,
    isAutoTranslateOn: Boolean,
    translationState: TranslationState,
    vibrateEnabled: Boolean,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onTranslateAndSend: () -> Unit,
    onToggleAutoTranslate: () -> Unit,
    onSwapLanguages: () -> Unit,
    onSelectFromLang: (String) -> Unit,
    onSelectToLang: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var layoutMode by remember { mutableStateOf(KeyboardLayoutMode.BANGLA) }
    var showQuickPhrases by remember { mutableStateOf(false) }
    var langPickerTarget by remember { mutableStateOf<String?>(null) } // "from" or "to" or null

    val fromLanguage = LanguageCatalog.findByCode(fromLangCode)
    val toLanguage = LanguageCatalog.findByCode(toLangCode)

    fun haptic() {
        if (vibrateEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("autolingo_keyboard_surface"),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP TRANSLATION TOOLBAR
            KeyboardTopBar(
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                isAutoOn = isAutoTranslateOn,
                onSwapLanguages = {
                    haptic()
                    onSwapLanguages()
                },
                onFromClick = { langPickerTarget = "from" },
                onToClick = { langPickerTarget = "to" },
                onAutoToggle = {
                    haptic()
                    onToggleAutoTranslate()
                },
                onQuickPhrasesToggle = {
                    haptic()
                    showQuickPhrases = !showQuickPhrases
                },
                onSettingsClick = {
                    haptic()
                    onOpenSettings()
                }
            )

            // Translation Progress / State Banner
            AnimatedVisibility(visible = translationState is TranslationState.Translating) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Translating ${fromLanguage.name} ➔ ${toLanguage.name}...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            AnimatedVisibility(visible = translationState is TranslationState.Error) {
                val errorMsg = (translationState as? TranslationState.Error)?.errorMessage.orEmpty()
                Text(
                    text = "⚠️ $errorMsg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Quick Phrases Bar
            AnimatedVisibility(visible = showQuickPhrases) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    KeyboardLayouts.quickBanglaPhrases.forEach { phrase ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.clickable {
                                haptic()
                                onKeyPress(phrase + " ")
                            }
                        ) {
                            Text(
                                text = phrase,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 2. BANGLA KAR/MATRA ROW (Shown in Bangla modes)
            if (layoutMode == KeyboardLayoutMode.BANGLA || layoutMode == KeyboardLayoutMode.BANGLA_SHIFT) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KeyboardLayouts.banglaKarRow.forEach { kar ->
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    haptic()
                                    onKeyPress(kar)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = kar,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 3. KEYBOARD KEYS GRID
            when (layoutMode) {
                KeyboardLayoutMode.BANGLA -> {
                    BanglaKeyGrid(
                        row1 = KeyboardLayouts.banglaNormalRow1,
                        row2 = KeyboardLayouts.banglaNormalRow2,
                        row3 = KeyboardLayouts.banglaNormalRow3,
                        isShifted = false,
                        onShiftToggle = {
                            haptic()
                            layoutMode = KeyboardLayoutMode.BANGLA_SHIFT
                        },
                        onKeyPress = {
                            haptic()
                            onKeyPress(it)
                        },
                        onBackspace = {
                            haptic()
                            onBackspace()
                        }
                    )
                }
                KeyboardLayoutMode.BANGLA_SHIFT -> {
                    BanglaKeyGrid(
                        row1 = KeyboardLayouts.banglaShiftRow1,
                        row2 = KeyboardLayouts.banglaShiftRow2,
                        row3 = KeyboardLayouts.banglaShiftRow3,
                        isShifted = true,
                        onShiftToggle = {
                            haptic()
                            layoutMode = KeyboardLayoutMode.BANGLA
                        },
                        onKeyPress = {
                            haptic()
                            onKeyPress(it)
                        },
                        onBackspace = {
                            haptic()
                            onBackspace()
                        }
                    )
                }
                KeyboardLayoutMode.ENGLISH_LOWER -> {
                    EnglishKeyGrid(
                        isUpper = false,
                        onShiftToggle = {
                            haptic()
                            layoutMode = KeyboardLayoutMode.ENGLISH_UPPER
                        },
                        onKeyPress = {
                            haptic()
                            onKeyPress(it)
                        },
                        onBackspace = {
                            haptic()
                            onBackspace()
                        }
                    )
                }
                KeyboardLayoutMode.ENGLISH_UPPER -> {
                    EnglishKeyGrid(
                        isUpper = true,
                        onShiftToggle = {
                            haptic()
                            layoutMode = KeyboardLayoutMode.ENGLISH_LOWER
                        },
                        onKeyPress = {
                            haptic()
                            onKeyPress(it)
                        },
                        onBackspace = {
                            haptic()
                            onBackspace()
                        }
                    )
                }
                KeyboardLayoutMode.NUMBERS_SYMBOLS -> {
                    NumbersSymbolsKeyGrid(
                        onKeyPress = {
                            haptic()
                            onKeyPress(it)
                        },
                        onBackspace = {
                            haptic()
                            onBackspace()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. BOTTOM ACTION BAR
            BottomActionBar(
                layoutMode = layoutMode,
                isTranslating = translationState is TranslationState.Translating,
                onModeToggle = {
                    haptic()
                    layoutMode = when (layoutMode) {
                        KeyboardLayoutMode.NUMBERS_SYMBOLS -> KeyboardLayoutMode.BANGLA
                        else -> KeyboardLayoutMode.NUMBERS_SYMBOLS
                    }
                },
                onLanguageSwitch = {
                    haptic()
                    layoutMode = when (layoutMode) {
                        KeyboardLayoutMode.BANGLA, KeyboardLayoutMode.BANGLA_SHIFT -> KeyboardLayoutMode.ENGLISH_LOWER
                        else -> KeyboardLayoutMode.BANGLA
                    }
                },
                onSpace = {
                    haptic()
                    onSpace()
                },
                onTranslateAndSend = {
                    haptic()
                    onTranslateAndSend()
                },
                onEnter = {
                    haptic()
                    onEnter()
                }
            )
        }
    }

    // Language Picker Dialog
    langPickerTarget?.let { target ->
        LanguagePickerDialog(
            title = if (target == "from") "Select Input Language" else "Select Target Language",
            currentCode = if (target == "from") fromLangCode else toLangCode,
            onSelect = { selectedCode ->
                if (target == "from") onSelectFromLang(selectedCode) else onSelectToLang(selectedCode)
                langPickerTarget = null
            },
            onDismiss = { langPickerTarget = null }
        )
    }
}

@Composable
private fun KeyboardTopBar(
    fromLanguage: SupportedLanguage,
    toLanguage: SupportedLanguage,
    isAutoOn: Boolean,
    onSwapLanguages: () -> Unit,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onAutoToggle: () -> Unit,
    onQuickPhrasesToggle: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // From Language Chip
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .testTag("keyboard_from_lang_chip")
                .clickable { onFromClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${fromLanguage.flag} ${fromLanguage.name}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select language",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Swap Languages Button
        IconButton(
            onClick = onSwapLanguages,
            modifier = Modifier.size(28.dp).testTag("keyboard_swap_lang_button")
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap Languages",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        // To Language Chip
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .testTag("keyboard_to_lang_chip")
                .clickable { onToClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${toLanguage.flag} ${toLanguage.name}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select language",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // AUTO ON/OFF Toggle
        val autoBgColor by animateColorAsState(
            targetValue = if (isAutoOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            label = "auto_bg"
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = autoBgColor,
            modifier = Modifier
                .testTag("keyboard_auto_toggle_chip")
                .clickable { onAutoToggle() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isAutoOn) Color(0xFF00C853) else Color.Gray)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAutoOn) "AUTO ON" else "AUTO OFF",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAutoOn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Quick phrases toggle
        IconButton(
            onClick = onQuickPhrasesToggle,
            modifier = Modifier.size(28.dp)
        ) {
            Text(
                text = "ব",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Settings Icon
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BanglaKeyGrid(
    row1: List<String>,
    row2: List<String>,
    row3: List<String>,
    isShifted: Boolean,
    onShiftToggle: () -> Unit,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            row1.forEach { char ->
                KeyboardKey(
                    label = char,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(char) }
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            row2.forEach { char ->
                KeyboardKey(
                    label = char,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(char) }
                )
            }
        }

        // Row 3 (with Shift and Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift / Swaroborno toggle key
            FunctionKey(
                modifier = Modifier.weight(1.3f),
                isToggled = isShifted,
                onClick = onShiftToggle
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Shift",
                    tint = if (isShifted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            row3.forEach { char ->
                KeyboardKey(
                    label = char,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(char) }
                )
            }

            // Backspace key
            FunctionKey(
                modifier = Modifier.weight(1.3f),
                onClick = onBackspace
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EnglishKeyGrid(
    isUpper: Boolean,
    onShiftToggle: () -> Unit,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            KeyboardLayouts.englishRow1.forEach { char ->
                val text = if (isUpper) char.uppercase() else char
                KeyboardKey(
                    label = text,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(text) }
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            KeyboardLayouts.englishRow2.forEach { char ->
                val text = if (isUpper) char.uppercase() else char
                KeyboardKey(
                    label = text,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(text) }
                )
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FunctionKey(
                modifier = Modifier.weight(1.4f),
                isToggled = isUpper,
                onClick = onShiftToggle
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Shift",
                    tint = if (isUpper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            KeyboardLayouts.englishRow3.forEach { char ->
                val text = if (isUpper) char.uppercase() else char
                KeyboardKey(
                    label = text,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(text) }
                )
            }

            FunctionKey(
                modifier = Modifier.weight(1.4f),
                onClick = onBackspace
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun NumbersSymbolsKeyGrid(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: Numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            KeyboardLayouts.numbersRow1.forEach { char ->
                KeyboardKey(
                    label = char,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(char) }
                )
            }
        }

        // Row 2: Bengali Digits
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            KeyboardLayouts.banglaDigits.forEach { char ->
                KeyboardKey(
                    label = char,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(char) }
                )
            }
        }

        // Row 3: Symbols & Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeyboardLayouts.symbolsRow3.forEach { char ->
                KeyboardKey(
                    label = char,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(char) }
                )
            }

            FunctionKey(
                modifier = Modifier.weight(1.3f),
                onClick = onBackspace
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    layoutMode: KeyboardLayoutMode,
    isTranslating: Boolean,
    onModeToggle: () -> Unit,
    onLanguageSwitch: () -> Unit,
    onSpace: () -> Unit,
    onTranslateAndSend: () -> Unit,
    onEnter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode toggle (?123 / কখ / ABC)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .weight(1.3f)
                .height(44.dp)
                .clickable { onModeToggle() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (layoutMode == KeyboardLayoutMode.NUMBERS_SYMBOLS) "ABC" else "?123",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Language Switch Button (🌐 Change Lang between Bangla and English)
        val isBangla = layoutMode == KeyboardLayoutMode.BANGLA || layoutMode == KeyboardLayoutMode.BANGLA_SHIFT
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .weight(1.5f)
                .height(44.dp)
                .testTag("keyboard_lang_switch_button")
                .clickable { onLanguageSwitch() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Change Language",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (isBangla) "EN" else "বাংলা",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Spacebar
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .weight(3.5f)
                .height(44.dp)
                .testTag("keyboard_space_key")
                .clickable { onSpace() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isBangla) "স্পেস" else "Space",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 🚀 Translate & Send Button
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(2.6f)
                .height(44.dp)
                .testTag("keyboard_translate_send_button")
                .clickable(enabled = !isTranslating) { onTranslateAndSend() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "Translate and Send",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Translate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Enter key
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .weight(1.3f)
                .height(44.dp)
                .clickable { onEnter() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enter",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun KeyboardKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier
            .height(42.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FunctionKey(
    modifier: Modifier = Modifier,
    isToggled: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val bgColor = if (isToggled) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
            .height(42.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun LanguagePickerDialog(
    title: String,
    currentCode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .horizontalScroll(rememberScrollState(0), enabled = false)
                ) {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(LanguageCatalog.languages.size) { index ->
                            val lang = LanguageCatalog.languages[index]
                            val isSelected = lang.code.equals(currentCode, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(lang.code) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = lang.flag, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = lang.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = lang.nativeName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
