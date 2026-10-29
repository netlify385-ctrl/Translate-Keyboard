package com.lingobubble.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.R
import com.lingobubble.service.MyAccessibilityService
import com.lingobubble.translation.TranslationResult
import com.lingobubble.utils.LanguageList
import java.util.Locale

/**
 * Floating Translation Result Popup Card shown near the bubble on drop.
 */
class ResultPopupView(
    private val context: Context,
    private val onChangeLanguageRequested: () -> Unit
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var popupView: View? = null
    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    fun show(result: TranslationResult, anchorX: Int, anchorY: Int) {
        dismiss()

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val cardWidth = (screenWidth * 0.88f).toInt().coerceAtMost((380 * displayMetrics.density).toInt())

        val windowLayoutParams = WindowManager.LayoutParams(
            cardWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            // Place near drop point, keeping inside screen boundaries
            x = (anchorX - cardWidth / 2).coerceIn(24, (screenWidth - cardWidth - 24).coerceAtLeast(0))
            y = (anchorY + 80).coerceIn(80, (screenHeight - 480).coerceAtLeast(80))
        }

        val rootLayout = FrameLayout(context)

        // Card Container
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 28)

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 36f
                setColor(0xFF1E2235.toInt())
                setStroke(2, 0xFF3D4363.toInt())
            }
            background = bg
            elevation = 20f
        }

        // Header: Drag handle / Logo / Languages badge / Close button
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val logoIcon = ImageView(context).apply {
            setImageResource(R.mipmap.ic_launcher_round)
            val sz = (24 * displayMetrics.density).toInt()
            val imgLp = LinearLayout.LayoutParams(sz, sz).apply {
                marginEnd = 12
            }
            this.layoutParams = imgLp
        }

        val targetLangItem = LanguageList.getByCode(result.targetLangCode)
        val langBadge = TextView(context).apply {
            text = "${targetLangItem.flag} ${result.detectedSourceLang} ➔ ${targetLangItem.name}"
            textSize = 12f
            setTextColor(0xFF89B4FA.toInt())
            val badgeBg = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(0x2289B4FA.toInt())
            }
            background = badgeBg
            setPadding(16, 6, 16, 6)
            val badgeLp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            this.layoutParams = badgeLp
        }

        val closeBtn = TextView(context).apply {
            text = "✕"
            textSize = 18f
            setTextColor(0xFFA6ADC8.toInt())
            setPadding(16, 8, 8, 8)
            setOnClickListener { dismiss() }
        }

        header.addView(logoIcon)
        header.addView(langBadge)
        header.addView(closeBtn)
        card.addView(header)

        // Original Text Preview (small)
        val originalLabel = TextView(context).apply {
            text = "Original:"
            textSize = 11f
            setTextColor(0xFF9399B2.toInt())
            setPadding(0, 16, 0, 4)
        }
        val originalText = TextView(context).apply {
            text = if (result.originalText.length > 120) result.originalText.take(120) + "..." else result.originalText
            textSize = 13f
            setTextColor(0xFFBAC2DE.toInt())
        }
        card.addView(originalLabel)
        card.addView(originalText)

        // Divider
        val divider = View(context).apply {
            setBackgroundColor(0xFF313244.toInt())
            val dividerLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                topMargin = 16
                bottomMargin = 16
            }
            this.layoutParams = dividerLp
        }
        card.addView(divider)

        // Translated Text (Big, Bold, Scrollable)
        val scrollBox = ScrollView(context).apply {
            val scrollLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (140 * displayMetrics.density).toInt()
            )
            this.layoutParams = scrollLp
        }
        val translatedText = TextView(context).apply {
            text = result.translatedText
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setLineSpacing(6f, 1.1f)
        }
        scrollBox.addView(translatedText)
        card.addView(scrollBox)

        // Engine badge & Actions row
        val actionsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 16, 0, 0)
        }

        // Copy Button
        val copyBtn = TextView(context).apply {
            text = "📋 Copy"
            textSize = 12f
            setTextColor(0xFFA6E3A1.toInt())
            val b = GradientDrawable().apply {
                cornerRadius = 18f
                setColor(0x22A6E3A1.toInt())
            }
            background = b
            setPadding(18, 10, 18, 10)
            setOnClickListener {
                copyToClipboard(result.translatedText)
                Toast.makeText(context, "Copied translation!", Toast.LENGTH_SHORT).show()
            }
        }

        // Speak Button
        val speakBtn = TextView(context).apply {
            text = "🔊 Speak"
            textSize = 12f
            setTextColor(0xFFF9E2AF.toInt())
            val b = GradientDrawable().apply {
                cornerRadius = 18f
                setColor(0x22F9E2AF.toInt())
            }
            background = b
            setPadding(18, 10, 18, 10)
            val speakLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 12
            }
            this.layoutParams = speakLp
            setOnClickListener {
                speakText(result.translatedText, result.targetLangCode)
            }
        }

        // Change Language Button
        val changeLangBtn = TextView(context).apply {
            text = "🌐 Lang"
            textSize = 12f
            setTextColor(0xFFCBA6F7.toInt())
            val b = GradientDrawable().apply {
                cornerRadius = 18f
                setColor(0x22CBA6F7.toInt())
            }
            background = b
            setPadding(18, 10, 18, 10)
            val langLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 12
            }
            this.layoutParams = langLp
            setOnClickListener {
                dismiss()
                onChangeLanguageRequested()
            }
        }

        // Replace Button (if editable node exists at anchor)
        val replaceBtn = TextView(context).apply {
            text = "✍️ Replace"
            textSize = 12f
            setTextColor(0xFF89DCEB.toInt())
            val b = GradientDrawable().apply {
                cornerRadius = 18f
                setColor(0x2289DCEB.toInt())
            }
            background = b
            setPadding(18, 10, 18, 10)
            val repLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 12
            }
            this.layoutParams = repLp
            setOnClickListener {
                val replaced = MyAccessibilityService.replaceTextAt(anchorX, anchorY, result.translatedText)
                if (replaced) {
                    Toast.makeText(context, "Text replaced!", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    copyToClipboard(result.translatedText)
                    Toast.makeText(context, "Copied to clipboard (not editable)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        actionsRow.addView(copyBtn)
        actionsRow.addView(speakBtn)
        actionsRow.addView(changeLangBtn)
        actionsRow.addView(replaceBtn)
        card.addView(actionsRow)

        rootLayout.addView(card)

        // Make popup card draggable anywhere
        var startX = 0
        var startY = 0
        var touchDownX = 0f
        var touchDownY = 0f

        rootLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = windowLayoutParams.x
                    startY = windowLayoutParams.y
                    touchDownX = event.rawX
                    touchDownY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    windowLayoutParams.x = startX + (event.rawX - touchDownX).toInt()
                    windowLayoutParams.y = startY + (event.rawY - touchDownY).toInt()
                    try {
                        windowManager.updateViewLayout(rootLayout, windowLayoutParams)
                    } catch (_: Exception) {}
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(rootLayout, windowLayoutParams)
            popupView = rootLayout
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismiss() {
        popupView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {}
            popupView = null
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("LingoBubble Translation", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun speakText(text: String, langCode: String) {
        try {
            val locale = Locale(langCode)
            textToSpeech?.language = locale
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LingoBubbleTTS")
        } catch (e: Exception) {
            Toast.makeText(context, "TTS not supported for this language", Toast.LENGTH_SHORT).show()
        }
    }

    fun destroy() {
        dismiss()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
