package com.lingobubble.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Display
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lingobubble.service.MyAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/**
 * Screen Text Extractor using ML Kit Text Recognition (Method B Fallback).
 */
class ScreenTextExtractor(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Extracts text from a provided Bitmap using ML Kit OCR.
     */
    suspend fun extractTextFromBitmap(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.text.trim())
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "ML Kit OCR failed: ${e.message}")
                        continuation.resume("")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error initiating OCR", e)
                continuation.resume("")
            }
        }
    }

    /**
     * Captures a screenshot of the region around (centerX, centerY) using AccessibilityService
     * and performs OCR extraction.
     */
    suspend fun captureAndExtractAt(
        centerX: Int,
        centerY: Int,
        widthDp: Int = 220,
        heightDp: Int = 120
    ): String = withContext(Dispatchers.Main) {
        val a11y = MyAccessibilityService.instance
        if (a11y == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return@withContext ""
        }

        val fullScreenBitmap: Bitmap? = suspendCancellableCoroutine { cont ->
            a11y.takeScreenshot(
                Display.DEFAULT_DISPLAY,
                executor,
                object : android.accessibilityservice.AccessibilityService.TakeScreenshotCallback {
                    override fun onSuccess(screenshot: android.accessibilityservice.AccessibilityService.ScreenshotResult) {
                        val hardwareBuffer = screenshot.hardwareBuffer
                        val colorSpace = screenshot.colorSpace
                        val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                        hardwareBuffer.close()
                        // Copy to software bitmap so ML Kit can process it
                        val softwareBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, false)
                        cont.resume(softwareBitmap)
                    }

                    override fun onFailure(errorCode: Int) {
                        Log.w(TAG, "Screenshot capture failed with code $errorCode")
                        cont.resume(null)
                    }
                }
            )
        }

        if (fullScreenBitmap == null) return@withContext ""

        // Crop the targeted area (220dp x 120dp)
        val density = context.resources.displayMetrics.density
        val cropW = (widthDp * density).toInt()
        val cropH = (heightDp * density).toInt()

        val left = (centerX - cropW / 2).coerceIn(0, fullScreenBitmap.width - 1)
        val top = (centerY - cropH / 2).coerceIn(0, fullScreenBitmap.height - 1)
        val actualW = cropW.coerceAtMost(fullScreenBitmap.width - left)
        val actualH = cropH.coerceAtMost(fullScreenBitmap.height - top)

        if (actualW <= 10 || actualH <= 10) return@withContext ""

        val cropped = Bitmap.createBitmap(fullScreenBitmap, left, top, actualW, actualH)
        extractTextFromBitmap(cropped)
    }

    companion object {
        private const val TAG = "ScreenTextExtractor"
    }
}
