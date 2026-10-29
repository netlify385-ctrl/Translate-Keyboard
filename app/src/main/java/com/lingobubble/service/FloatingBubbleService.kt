package com.lingobubble.service

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.lingobubble.ocr.ScreenTextExtractor
import com.lingobubble.translation.GeminiTranslator
import com.lingobubble.ui.ResultPopupView
import com.lingobubble.utils.LanguageList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Foreground Service running the floating bubble (like Messenger Chat Head)
 * with drag-to-translate functionality across all Android apps.
 */
class FloatingBubbleService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var windowManager: WindowManager
    private lateinit var prefs: SharedPreferences
    private lateinit var translator: GeminiTranslator
    private lateinit var ocrExtractor: ScreenTextExtractor
    private lateinit var resultPopup: ResultPopupView

    private var bubbleRootView: FrameLayout? = null
    private var bubbleImageView: ImageView? = null
    private var bubbleProgressBar: ProgressBar? = null
    private var quickMenuLayout: LinearLayout? = null
    private var languageBarLayout: LinearLayout? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams

    private var screenWidth = 0
    private var screenHeight = 0
    private var baseBubbleSize = 56 // dp
    private var currentTargetLang = "en"

    // Gesture detection
    private var initialX = 0
    private var initialY = 0
    private var touchDownX = 0f
    private var touchDownY = 0f
    private var touchDownTime = 0L
    private var isDragging = false
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    private val longPressRunnable = Runnable {
        if (!isDragging) {
            showQuickMenu()
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences("lingobubble_prefs", Context.MODE_PRIVATE)
        currentTargetLang = prefs.getString("target_lang", "en") ?: "en"

        translator = GeminiTranslator(this)
        ocrExtractor = ScreenTextExtractor(this)
        resultPopup = ResultPopupView(this) {
            showLanguageSelector()
        }

        updateScreenMetrics()
        startForegroundNotification()
        createFloatingBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("set_target_lang")?.let {
            currentTargetLang = it
            prefs.edit().putString("target_lang", it).apply()
        }
        return START_STICKY
    }

    private fun updateScreenMetrics() {
        val dm = resources.displayMetrics
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels
    }

    private fun startForegroundNotification() {
        val channelId = "lingobubble_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "LingoBubble Active Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the drag-to-translate floating bubble active"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("LingoBubble is active")
            .setContentText("Drag bubble onto any text on screen to translate")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingBubble() {
        if (!Settings.canDrawOverlays(this)) return

        val density = resources.displayMetrics.density
        val sizePx = (baseBubbleSize * density).toInt()

        bubbleParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = screenHeight / 3
        }

        val root = FrameLayout(this)
        bubbleRootView = root

        // Circular background with subtle glow & shadow
        val circleBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            colors = intArrayOf(0xFF38BDF8.toInt(), 0xFF6366F1.toInt())
            gradientType = GradientDrawable.LINEAR_GRADIENT
            orientation = GradientDrawable.Orientation.TL_BR
            setStroke(3, 0xFFE0E7FF.toInt())
        }
        root.background = circleBg
        root.elevation = 22f

        // Icon inside bubble
        val icon = ImageView(this).apply {
            setImageResource(R.mipmap.ic_launcher_round)
            val padding = (8 * density).toInt()
            setPadding(padding, padding, padding, padding)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        bubbleImageView = icon
        root.addView(icon)

        // Loading spinner for Dropped state
        val spinner = ProgressBar(this).apply {
            visibility = View.GONE
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        bubbleProgressBar = spinner
        root.addView(spinner)

        // Touch Listener for Drag, Snap, Tap, and Long-press
        root.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bubbleParams.x
                    initialY = bubbleParams.y
                    touchDownX = event.rawX
                    touchDownY = event.rawY
                    touchDownTime = System.currentTimeMillis()
                    isDragging = false

                    // Enlarge bubble on touch (Dragging state)
                    animateBubbleScale(1.18f)
                    mainHandler.postDelayed(longPressRunnable, longPressTimeout)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - touchDownX).toInt()
                    val deltaY = (event.rawY - touchDownY).toInt()

                    if (Math.abs(deltaX) > 12 || Math.abs(deltaY) > 12) {
                        isDragging = true
                        mainHandler.removeCallbacks(longPressRunnable)
                        dismissQuickMenu()
                        dismissLanguageSelector()
                    }

                    if (isDragging) {
                        bubbleParams.x = initialX + deltaX
                        bubbleParams.y = initialY + deltaY
                        try {
                            windowManager.updateViewLayout(root, bubbleParams)
                        } catch (_: Exception) {}
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    mainHandler.removeCallbacks(longPressRunnable)
                    animateBubbleScale(1.0f)

                    val duration = System.currentTimeMillis() - touchDownTime
                    val deltaX = Math.abs(event.rawX - touchDownX)
                    val deltaY = Math.abs(event.rawY - touchDownY)

                    if (!isDragging && deltaX < 15 && deltaY < 15 && duration < longPressTimeout) {
                        // Single tap: open language picker
                        onBubbleSingleTap()
                    } else if (isDragging) {
                        // Dropped onto screen: process text capture & translate
                        val dropX = bubbleParams.x + (bubbleParams.width / 2)
                        val dropY = bubbleParams.y + (bubbleParams.height / 2)
                        onBubbleDropped(dropX, dropY)
                    }
                    true
                }

                else -> false
            }
        }

        try {
            windowManager.addView(root, bubbleParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun animateBubbleScale(scale: Float) {
        bubbleRootView?.animate()
            ?.scaleX(scale)
            ?.scaleY(scale)
            ?.setDuration(120)
            ?.start()
    }

    private fun snapToNearestEdge() {
        val root = bubbleRootView ?: return
        val currentX = bubbleParams.x
        val targetX = if (currentX + (bubbleParams.width / 2) < screenWidth / 2) {
            16
        } else {
            screenWidth - bubbleParams.width - 16
        }

        val animator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 240
            interpolator = DecelerateInterpolator()
            addUpdateListener { va ->
                bubbleParams.x = va.animatedValue as Int
                try {
                    windowManager.updateViewLayout(root, bubbleParams)
                } catch (_: Exception) {}
            }
        }
        animator.start()
    }

    private fun onBubbleSingleTap() {
        showLanguageSelector()
    }

    /**
     * Text capture on drop: combines Method A (Accessibility) and Method B (ML Kit OCR)
     */
    private fun onBubbleDropped(dropX: Int, dropY: Int) {
        // Show loading spinner on bubble
        setBubbleState(State.DROPPED_LOADING)

        serviceScope.launch {
            // Method A (Primary): Accessibility Service node inspection
            var capturedText = MyAccessibilityService.findTextAt(dropX, dropY)

            // Method B (Fallback): Screenshot + ML Kit Text Recognition
            if (capturedText.isNullOrBlank()) {
                capturedText = ocrExtractor.captureAndExtractAt(dropX, dropY)
            }

            if (capturedText.isNullOrBlank()) {
                // If nothing was captured under drop point, snap back with hint
                withContext(Dispatchers.Main) {
                    setBubbleState(State.IDLE)
                    snapToNearestEdge()
                    Toast.makeText(
                        this@FloatingBubbleService,
                        "Drop directly over any text to translate!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            // Translate captured text with Gemini / fallback
            val apiKey = prefs.getString("gemini_api_key", "") ?: ""
            val engine = prefs.getString("preferred_engine", "gemini") ?: "gemini"

            val result = translator.translate(
                capturedText = capturedText,
                targetLangCode = currentTargetLang,
                apiKey = apiKey,
                preferredEngine = engine
            )

            withContext(Dispatchers.Main) {
                setBubbleState(State.IDLE)
                snapToNearestEdge()

                if (result.isSuccess) {
                    val translationResult = result.getOrNull()!!
                    resultPopup.show(translationResult, dropX, dropY)
                } else {
                    val err = result.exceptionOrNull()?.localizedMessage ?: "Translation failed"
                    Toast.makeText(this@FloatingBubbleService, "Translation error: $err", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private enum class State { IDLE, DRAGGING, DROPPED_LOADING }

    private fun setBubbleState(state: State) {
        when (state) {
            State.DROPPED_LOADING -> {
                bubbleImageView?.visibility = View.INVISIBLE
                bubbleProgressBar?.visibility = View.VISIBLE
            }
            else -> {
                bubbleImageView?.visibility = View.VISIBLE
                bubbleProgressBar?.visibility = View.GONE
            }
        }
    }

    /**
     * Quick Language Selector bar shown on tap
     */
    private fun showLanguageSelector() {
        dismissLanguageSelector()
        dismissQuickMenu()

        val root = bubbleRootView ?: return
        val density = resources.displayMetrics.density

        val barParams = WindowManager.LayoutParams(
            (screenWidth * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (bubbleParams.y + (70 * density).toInt()).coerceIn(60, screenHeight - 200)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            val bg = GradientDrawable().apply {
                cornerRadius = 24f
                setColor(0xEE1E2235.toInt())
                setStroke(2, 0xFF434968.toInt())
            }
            background = bg
            elevation = 24f
        }

        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            val curr = LanguageList.getByCode(currentTargetLang)
            text = "Translate to: ${curr.flag} ${curr.name}"
            textSize = 13f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val close = TextView(this).apply {
            text = "✕"
            textSize = 16f
            setTextColor(0xFFA6ADC8.toInt())
            setPadding(12, 4, 12, 4)
            setOnClickListener { dismissLanguageSelector() }
        }
        headerRow.addView(title)
        headerRow.addView(close)
        container.addView(headerRow)

        // Horizontal language scroll list
        val scrollView = HorizontalScrollView(this).apply {
            setPadding(0, 12, 0, 4)
        }
        val listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        LanguageList.languages.forEach { lang ->
            val isSelected = lang.code.equals(currentTargetLang, ignoreCase = true)
            val chip = TextView(this).apply {
                text = "${lang.flag} ${lang.name}"
                textSize = 12f
                setTextColor(if (isSelected) Color.WHITE else 0xFFCDD6F4.toInt())
                val chipBg = GradientDrawable().apply {
                    cornerRadius = 16f
                    setColor(if (isSelected) 0xFF4F46E5.toInt() else 0xFF2A2E45.toInt())
                    if (isSelected) setStroke(2, Color.WHITE)
                }
                background = chipBg
                setPadding(22, 10, 22, 10)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 10
                }
                layoutParams = params

                setOnClickListener {
                    currentTargetLang = lang.code
                    prefs.edit().putString("target_lang", lang.code).apply()
                    Toast.makeText(this@FloatingBubbleService, "Target: ${lang.flag} ${lang.name}", Toast.LENGTH_SHORT).show()
                    dismissLanguageSelector()
                }
            }
            listLayout.addView(chip)
        }

        scrollView.addView(listLayout)
        container.addView(scrollView)

        try {
            windowManager.addView(container, barParams)
            languageBarLayout = container
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dismissLanguageSelector() {
        languageBarLayout?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
            languageBarLayout = null
        }
    }

    /**
     * Long-press menu: [Settings] [Hide Bubble] [Exit]
     */
    private fun showQuickMenu() {
        dismissQuickMenu()
        val density = resources.displayMetrics.density

        val menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (bubbleParams.x + (60 * density).toInt()).coerceAtMost(screenWidth - (200 * density).toInt())
            y = bubbleParams.y
        }

        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            val bg = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(0xF0181825.toInt())
                setStroke(2, 0xFF45475A.toInt())
            }
            background = bg
            elevation = 26f
        }

        // Settings Button
        val settingsItem = createMenuItem("⚙️ Settings") {
            dismissQuickMenu()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }

        // Hide Bubble Button
        val hideItem = createMenuItem("👁️ Hide for now") {
            dismissQuickMenu()
            bubbleRootView?.visibility = View.GONE
            Toast.makeText(this, "Bubble hidden. Open app to restore.", Toast.LENGTH_LONG).show()
        }

        // Exit Button
        val exitItem = createMenuItem("✕ Exit Service") {
            dismissQuickMenu()
            stopSelf()
        }

        menu.addView(settingsItem)
        menu.addView(hideItem)
        menu.addView(exitItem)

        try {
            windowManager.addView(menu, menuParams)
            quickMenuLayout = menu
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createMenuItem(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(24, 14, 24, 14)
            setOnClickListener { onClick() }
        }
    }

    private fun dismissQuickMenu() {
        quickMenuLayout?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
            quickMenuLayout = null
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        dismissLanguageSelector()
        dismissQuickMenu()
        resultPopup.destroy()
        bubbleRootView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
            bubbleRootView = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java)
            context.stopService(intent)
        }
    }
}
