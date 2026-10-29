package com.lingobubble.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Accessibility Service for capturing text under the floating bubble drop coordinates (Method A).
 */
class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "MyAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Event processing if needed
    }

    override fun onInterrupt() {
        Log.d(TAG, "MyAccessibilityService interrupted")
    }

    override fun onDestroy() {
        if (instance == this) {
            instance = null
        }
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MyA11yService"
        @Volatile
        var instance: MyAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null

        /**
         * Inspects the view hierarchy under the given screen (X, Y) coordinates
         * and returns the detected text.
         */
        fun findTextAt(screenX: Int, screenY: Int): String? {
            val service = instance ?: return null
            val root = service.rootInActiveWindow ?: return null

            val matchingNodes = mutableListOf<NodeMatch>()
            findMatchingNodes(root, screenX, screenY, matchingNodes)

            if (matchingNodes.isEmpty()) return null

            // Sort by smallest bounding box area (most specific leaf view target)
            matchingNodes.sortBy { it.area }

            for (match in matchingNodes) {
                val text = match.text
                if (text.isNotBlank() && text.length >= 2) {
                    return text
                }
            }

            return null
        }

        /**
         * Optional: Replaces text in an editable node under coordinates if supported.
         */
        fun replaceTextAt(screenX: Int, screenY: Int, newText: String): Boolean {
            val service = instance ?: return false
            val root = service.rootInActiveWindow ?: return false

            val matchingNodes = mutableListOf<NodeMatch>()
            findMatchingNodes(root, screenX, screenY, matchingNodes)
            matchingNodes.sortBy { it.area }

            val editable = matchingNodes.firstOrNull { it.node.isEditable }?.node
            if (editable != null) {
                val arguments = Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        newText
                    )
                }
                return editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            }
            return false
        }

        private fun findMatchingNodes(
            node: AccessibilityNodeInfo?,
            x: Int,
            y: Int,
            results: MutableList<NodeMatch>
        ) {
            if (node == null) return

            val rect = Rect()
            node.getBoundsInScreen(rect)

            // Tolerance margin of 24 pixels around bubble center
            val tolerance = 24
            val expandedRect = Rect(
                rect.left - tolerance,
                rect.top - tolerance,
                rect.right + tolerance,
                rect.bottom + tolerance
            )

            if (expandedRect.contains(x, y)) {
                val rawText = node.text?.toString()?.trim()
                    ?: node.contentDescription?.toString()?.trim()

                if (!rawText.isNullOrEmpty()) {
                    val area = (rect.width().coerceAtLeast(1)) * (rect.height().coerceAtLeast(1))
                    results.add(NodeMatch(node, rawText, area))
                }
            }

            for (i in 0 until node.childCount) {
                findMatchingNodes(node.getChild(i), x, y, results)
            }
        }

        private data class NodeMatch(
            val node: AccessibilityNodeInfo,
            val text: String,
            val area: Int
        )
    }
}
