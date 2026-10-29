# LingoBubble - Drag to Translate

**LingoBubble** is a floating screen translator inspired by Hi Translate and Facebook Messenger Chat Heads. It allows users to drag a floating bubble anywhere on screen and drop it directly onto any text in WhatsApp, Telegram, Messenger, Facebook, Instagram, or any Android app to instantly capture (via Accessibility + ML Kit OCR) and translate the text using Google Gemini AI and multilingual translation APIs.

---

## 🌟 Key Features

1. **Floating Bubble (Messenger-style Chat Head)**:
   - Always accessible floating on top of all apps.
   - Smooth 60fps drag with dynamic scaling and magnetic edge snapping.
   - 3 States:
     - **Idle**: Compact translucent badge resting on the screen edge.
     - **Dragging**: Enlarges and follows touch smoothly.
     - **Dropped**: Displays animated loading indicator while capturing and translating.

2. **Dual-Method Text Capture on Drop**:
   - **Method A (Primary)**: Uses `AccessibilityService` (`AccessibilityNodeInfo`) to inspect the exact view hierarchy coordinates under the bubble drop location.
   - **Method B (Fallback)**: Uses `AccessibilityService.takeScreenshot()` combined with Google **ML Kit Text Recognition** (`com.google.mlkit:text-recognition`) to OCR text from screenshots when views are custom or canvas-rendered.

3. **Gemini AI Instant Translation (100+ Languages)**:
   - High-quality natural language translation powered by Google Gemini 1.5 Flash (`gemini-1.5-flash`).
   - Seamless free fallback via MyMemory API if no API key is entered.
   - Complete catalog of 100+ global languages with flags and native names.

4. **Interactive Result Popup Card**:
   - Shows detected source language and target language banner.
   - Bold, clear translated text with vertical scrolling.
   - Action buttons:
     - **📋 Copy**: Copies translated text to clipboard.
     - **🔊 Speak**: Text-to-Speech (TTS) pronunciation.
     - **🌐 Lang**: Quick language switcher.
     - **✍️ Replace**: Automatically replaces text in editable text fields if supported.
     - **✕ Close**: Dismisses card.
   - Fully draggable across the screen.

5. **Quick Bubble Menu**:
   - **Single tap**: Horizontal quick language picker.
   - **Long press**: Menu with `[⚙️ Settings]`, `[👁️ Hide for now]`, and `[✕ Exit Service]`.

---

## 🚀 How to Enable Required Permissions

### 1. Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)
1. Open **LingoBubble**.
2. Under **Setup & Permissions**, tap **"Enable"** next to **1. Display Over Apps**.
3. In the Android Settings screen that appears, find **LingoBubble** and toggle **"Allow display over other apps"** to **ON**.
4. Return to LingoBubble.

### 2. Accessibility Service (`BIND_ACCESSIBILITY_SERVICE`)
1. In **LingoBubble**, tap **"Enable"** next to **2. Accessibility Service**.
2. In the Accessibility settings list, scroll down to **Downloaded Apps / Installed Services**.
3. Tap **"LingoBubble Screen Text Capture"**.
4. Toggle the switch to **ON** and accept the confirmation prompt.
5. Return to LingoBubble.

---

## 🧪 Testing with the Built-in Arena

1. Launch **LingoBubble** and ensure both permissions are enabled.
2. Tap **"Start Bubble"**. The floating bubble will appear on screen.
3. Scroll down to the **Drag & Drop Live Testing Arena**.
4. Press and drag the bubble over any sample card (Spanish, Bangla, German, Japanese, Arabic) and release (drop) it directly over the text.
5. The bubble will show the loading spinner, capture the text, and pop up the translation card with copy, speech, and language selection controls.
