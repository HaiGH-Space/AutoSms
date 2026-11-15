package com.gmail.nthai.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ZaloAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
        Log.d(TAG, "ZaloAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName != "com.zing.zalo") return

        val rootNode = rootInActiveWindow ?: return

        // If auto-send is active, attempt to execute pending send actions
        if (isPendingActionActive) {
            handleAutoSendFlow(rootNode)
        }
    }

    private fun handleAutoSendFlow(rootNode: AccessibilityNodeInfo) {
        val message = currentPendingMessage ?: return

        // 1. Check & click "Kết bạn" if autoAddFriend is enabled and button exists
        if (currentAutoAddFriend) {
            findAndClickAddFriendButton(rootNode)
        }

        // 2. Find message input field & set text
        val inputNode = findMessageInputNode(rootNode)
        if (inputNode != null) {
            val arguments = Bundle().apply {
                putCharSequence("ACTION_ARGUMENT_SET_TEXT_ARGUMENT", message)
            }
            val textSet = inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            Log.d(TAG, "Text set result: $textSet for message: $message")

            // 3. Find and click Send button
            val sendClicked = findAndClickSendButton(rootNode)
            if (sendClicked) {
                Log.d(TAG, "Send button clicked successfully")
                isPendingActionActive = false
                currentPendingMessage = null
                _sendEvents.tryEmit(SendResult.Success)
            }
        }
    }

    private fun findAndClickAddFriendButton(rootNode: AccessibilityNodeInfo): Boolean {
        val addFriendNodes = rootNode.findAccessibilityNodeInfosByText("Kết bạn")
        if (!addFriendNodes.isNullOrEmpty()) {
            for (node in addFriendNodes) {
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d(TAG, "Clicked Kết bạn button directly")
                    return true
                } else {
                    var parent = node.parent
                    while (parent != null) {
                        if (parent.isClickable) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Log.d(TAG, "Clicked Kết bạn parent button")
                            return true
                        }
                        parent = parent.parent
                    }
                }
            }
        }
        return false
    }

    private fun findMessageInputNode(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Try finding EditText by class name
        val editTexts = mutableListOf<AccessibilityNodeInfo>()
        findNodesByClassName(rootNode, "android.widget.EditText", editTexts)
        if (editTexts.isNotEmpty()) {
            return editTexts.first()
        }

        // Try finding by hint/text
        val hintNodes = rootNode.findAccessibilityNodeInfosByText("Nhập tin nhắn")
        if (!hintNodes.isNullOrEmpty()) {
            return hintNodes.first()
        }

        return null
    }

    private fun findAndClickSendButton(rootNode: AccessibilityNodeInfo): Boolean {
        // Common Zalo send button texts & content descriptions
        val sendIdentifiers = listOf("Gửi", "Send", "btn_send", "chat_input_send_btn")
        for (identifier in sendIdentifiers) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(identifier)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    if (node.isClickable) {
                        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    var parent = node.parent
                    while (parent != null) {
                        if (parent.isClickable) {
                            return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                        parent = parent.parent
                    }
                }
            }
        }

        // Fallback: search for last image/button view in window content
        val imageViews = mutableListOf<AccessibilityNodeInfo>()
        findNodesByClassName(rootNode, "android.widget.ImageView", imageViews)
        for (iv in imageViews.reversed()) {
            val cd = iv.contentDescription?.toString() ?: ""
            if (cd.contains("Gửi", ignoreCase = true) || cd.contains("Send", ignoreCase = true)) {
                if (iv.isClickable) return iv.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                var p = iv.parent
                while (p != null) {
                    if (p.isClickable) return p.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    p = p.parent
                }
            }
        }

        return false
    }

    private fun findNodesByClassName(
        root: AccessibilityNodeInfo,
        targetClass: String,
        results: MutableList<AccessibilityNodeInfo>
    ) {
        if (root.className != null && root.className.toString() == targetClass) {
            results.add(root)
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            findNodesByClassName(child, targetClass, results)
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "ZaloAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceActive.value = false
        Log.d(TAG, "ZaloAccessibilityService destroyed")
    }

    sealed class SendResult {
        data object Success : SendResult()
        data class Error(val reason: String) : SendResult()
    }

    companion object {
        private const val TAG = "ZaloAccessibility"
        private var instance: ZaloAccessibilityService? = null

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _sendEvents = MutableSharedFlow<SendResult>(extraBufferCapacity = 10)
        val sendEvents: SharedFlow<SendResult> = _sendEvents.asSharedFlow()

        private var isPendingActionActive = false
        private var currentPendingMessage: String? = null
        private var currentAutoAddFriend = false

        fun isAccessibilityEnabled(context: Context): Boolean {
            val am =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                    ?: return false
            val enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            val isEnabledInList = enabledServices.any {
                it.resolveInfo.serviceInfo.packageName == context.packageName &&
                        it.resolveInfo.serviceInfo.name == ZaloAccessibilityService::class.java.name
            }
            if (isEnabledInList) return true

            // Fallback check via Settings.Secure
            val prefString = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(prefString)

            val expectedComponentName =
                "${context.packageName}/${ZaloAccessibilityService::class.java.name}"
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }

        fun prepareAutoSend(messageText: String, autoAddFriend: Boolean) {
            currentPendingMessage = messageText
            currentAutoAddFriend = autoAddFriend
            isPendingActionActive = true
        }

        fun clearPendingAction() {
            isPendingActionActive = false
            currentPendingMessage = null
        }
    }
}
