package com.example.service

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.R

/**
 * Manages floating translation bubble on top of other apps (WhatsApp, Telegram, etc.)
 */
class FloatingBubbleManager(private val context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var bubbleView: View? = null

    @SuppressLint("ClickableViewAccessibility")
    fun showBubble(originalText: String, translatedBanglaText: String) {
        if (!Settings.canDrawOverlays(context)) {
            return
        }

        mainHandler.post {
            // If already shown, update text and reset timer
            if (bubbleView != null) {
                updateBubbleContent(originalText, translatedBanglaText)
                return@post
            }

            val windowParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
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
                x = 30
                y = 200
            }

            val rootLayout = FrameLayout(context)

            // Content container with rounded background
            val contentBox = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 24, 32, 24)

                val backgroundShape = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 32f
                    setColor(0xFF1E1E2E.toInt())
                    setStroke(2, 0xFF313244.toInt())
                }
                background = backgroundShape
            }

            // Top Header: Icon + App Name + Close button
            val headerRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val icon = ImageView(context).apply {
                setImageResource(R.mipmap.ic_launcher_round)
                val iconSize = 48
                val imgParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                    marginEnd = 16
                }
                this.layoutParams = imgParams
            }

            val titleText = TextView(context).apply {
                text = "AutoLingo Translation 🇧🇩"
                textSize = 13f
                setTextColor(0xFF89B4FA.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val closeBtn = TextView(context).apply {
                text = "✕"
                textSize = 16f
                setTextColor(0xFFA6ADC8.toInt())
                setPadding(12, 4, 12, 4)
                setOnClickListener {
                    dismissBubble()
                }
            }

            headerRow.addView(icon)
            headerRow.addView(titleText)
            headerRow.addView(closeBtn)

            // Translated Text in Bangla
            val translatedView = TextView(context).apply {
                tag = "tag_translated_text"
                text = translatedBanglaText
                textSize = 15f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 12, 0, 8)
            }

            // Original Text in subtle color
            val originalView = TextView(context).apply {
                tag = "tag_original_text"
                text = "Original: $originalText"
                textSize = 11f
                setTextColor(0xFFBAC2DE.toInt())
            }

            // Tap-to-Copy Action Bar
            val actionRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
                setPadding(0, 12, 0, 0)
            }

            val copyBtn = TextView(context).apply {
                text = "📋 Copy Bangla"
                textSize = 12f
                setTextColor(0xFFA6E3A1.toInt())
                val copyBtnShape = GradientDrawable().apply {
                    cornerRadius = 16f
                    setColor(0x22A6E3A1.toInt())
                }
                background = copyBtnShape
                setPadding(20, 8, 20, 8)
                setOnClickListener {
                    copyToClipboard(translatedBanglaText)
                    Toast.makeText(context, "Copied Bangla translation!", Toast.LENGTH_SHORT).show()
                }
            }

            actionRow.addView(copyBtn)

            contentBox.addView(headerRow)
            contentBox.addView(translatedView)
            contentBox.addView(originalView)
            contentBox.addView(actionRow)

            rootLayout.addView(contentBox)

            // Drag handling on touch
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f

            rootLayout.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = windowParams.x
                        initialY = windowParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        windowParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        windowParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        try {
                            windowManager.updateViewLayout(rootLayout, windowParams)
                        } catch (_: Exception) {}
                        true
                    }
                    else -> false
                }
            }

            try {
                windowManager.addView(rootLayout, windowParams)
                bubbleView = rootLayout
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateBubbleContent(originalText: String, translatedBanglaText: String) {
        val root = bubbleView ?: return
        val transView = root.findViewWithTag<TextView>("tag_translated_text")
        val origView = root.findViewWithTag<TextView>("tag_original_text")
        transView?.text = translatedBanglaText
        origView?.text = "Original: $originalText"
    }

    fun dismissBubble() {
        mainHandler.post {
            bubbleView?.let { view ->
                try {
                    windowManager.removeView(view)
                } catch (_: Exception) {}
                bubbleView = null
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AutoLingo Translation", text)
        clipboard.setPrimaryClip(clip)
    }
}
