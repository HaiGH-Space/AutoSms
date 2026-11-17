package com.gmail.nthai.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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

@SuppressLint("AccessibilityPolicy")
class ZaloAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
        Log.d(TAG, "ZaloAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        // If returned to AutoSms, stop returning process immediately
        if (packageName == applicationContext.packageName) {
            isReturningToApp = false
            return
        }

        if (packageName != "com.zing.zalo") return

        val rootNode = rootInActiveWindow ?: return

        // If auto-send is active, attempt to execute pending send actions
        if (isPendingActionActive) {
            // First check if Zalo opened a WebView (indicating web error page / profile not found / invalid number)
            if (handleWebViewError(rootNode)) {
                return
            }
            // Then check if Zalo is showing user/page not found error screen via text
            if (handleNotFoundScreen(rootNode)) {
                return
            }
            handleAutoSendFlow(rootNode)
        }
    }

    private fun handleWebViewError(rootNode: AccessibilityNodeInfo): Boolean {
        val webViews = mutableListOf<AccessibilityNodeInfo>()
        findNodesByClassName(rootNode, "android.webkit.WebView", webViews)
        if (webViews.isNotEmpty()) {
            Log.w(TAG, "Detected Zalo WebView error/web page screen (invalid number)")
            isPendingActionActive = false
            currentPendingMessage = null
            _sendEvents.tryEmit(SendResult.NotFound(reason = "Số điện thoại chưa đăng ký Zalo hoặc không hợp lệ (WebView)"))
            return true
        }
        return false
    }

    private fun handleNotFoundScreen(rootNode: AccessibilityNodeInfo): Boolean {
        val notFoundTexts = listOf(
            "Trang này không tìm thấy hoặc không hợp lệ",
            "Không tìm thấy tài khoản",
            "Số điện thoại chưa đăng ký Zalo",
            "Không tìm thấy người dùng",
            "Tài khoản không tồn tại",
            "Không tìm thấy kết quả",
            "Số điện thoại này không sử dụng Zalo",
            "Đường dẫn không hợp lệ",
            "Trang này không tồn tại",
        )

        for (text in notFoundTexts) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(text)
            if (!nodes.isNullOrEmpty()) {
                Log.w(TAG, "Detected not found screen with text: $text")
                isPendingActionActive = false
                currentPendingMessage = null
                _sendEvents.tryEmit(SendResult.NotFound(reason = text))
                return true
            }
        }
        return false
    }


    @Suppress("UsePropertyAccessSyntax")
    private fun handleAutoSendFlow(rootNode: AccessibilityNodeInfo) {
        if (isActionInProgress) return
        val message = currentPendingMessage ?: return

        // 0. Check & click "Nhắn tin" (Chat) button if on profile view
        if (findAndClickChatButton(rootNode)) {
            Log.d(TAG, "Clicked 'Nhắn tin' button, waiting for chat screen...")
            return
        }

        // 1. Check & click "Kết bạn" if autoAddFriend is enabled and button exists
        if (currentAutoAddFriend) {
            findAndClickAddFriendButton(rootNode)
        }

        // 2. Find message input field & set/paste text
        val inputNode = findMessageInputNode(rootNode)
        if (inputNode != null) {
            isActionInProgress = true

            inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            inputNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

            // Try SET_TEXT
            val arguments = Bundle().apply {
                putCharSequence("ACTION_ARGUMENT_SET_TEXT_ARGUMENT", message)
            }
            inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

            // Also try CLIPBOARD PASTE as a robust fallback for custom views
            try {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("ZaloMessage", message))
                inputNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                Log.d(TAG, "Performed ACTION_PASTE on input node")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to paste via clipboard", e)
            }

            Log.d(TAG, "Text set/pasted. Initiating Send button click retry loop...")

            val maxRetries = if (currentIsLowSpecDevice) 6 else 4
            val retryDelayMs = if (currentIsLowSpecDevice) 500L else 300L
            val postSendDelayMs = if (currentIsLowSpecDevice) 1000L else 600L

            attemptClickSendWithRetry(
                retryCount = 1,
                maxRetries = maxRetries,
                retryDelayMs = retryDelayMs,
                postSendDelayMs = postSendDelayMs
            )
        }
    }

    private fun attemptClickSendWithRetry(
        retryCount: Int,
        maxRetries: Int,
        retryDelayMs: Long,
        postSendDelayMs: Long
    ) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
            {
                if (!isPendingActionActive) {
                    isActionInProgress = false
                    return@postDelayed
                }

                val latestRoot = rootInActiveWindow
                val sendClicked = latestRoot?.let { findAndClickSendButton(it) } ?: false
                Log.d(TAG, "Send button click attempt #$retryCount result: $sendClicked")

                if (sendClicked) {
                    isPendingActionActive = false
                    currentPendingMessage = null
                    isActionInProgress = false
                    _sendEvents.tryEmit(SendResult.Success)
                } else {
                    if (retryCount < maxRetries) {
                        Log.d(TAG, "Retrying send button click ($retryCount/$maxRetries)...")
                        attemptClickSendWithRetry(
                            retryCount = retryCount + 1,
                            maxRetries = maxRetries,
                            retryDelayMs = retryDelayMs,
                            postSendDelayMs = postSendDelayMs
                        )
                    } else {
                        Log.w(TAG, "Failed to find/click Send button after $maxRetries retries")
                        isPendingActionActive = false
                        currentPendingMessage = null
                        _sendEvents.tryEmit(SendResult.Error("Không tìm thấy nút gửi trên Zalo"))
                        isActionInProgress = false
                    }
                }
            },
            retryDelayMs
        )
    }

    private fun findAndClickChatButton(rootNode: AccessibilityNodeInfo): Boolean {
        val chatIdentifiers = listOf("Nhắn tin", "Chat")
        for (identifier in chatIdentifiers) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(identifier)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    val text = node.text?.toString() ?: ""
                    val cd = node.contentDescription?.toString() ?: ""
                    if (text.equals(identifier, ignoreCase = true) || cd.equals(
                            identifier,
                            ignoreCase = true,
                        ) || text.contains(identifier, ignoreCase = true) || cd.contains(
                            identifier,
                            ignoreCase = true,
                        )
                    ) {
                        if (node.isClickable) {
                            Log.d(TAG, "Clicked Chat button directly: $identifier")
                            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                        var parent = node.parent
                        while (parent != null) {
                            if (parent.isClickable) {
                                Log.d(TAG, "Clicked Chat parent button: $identifier")
                                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                            parent = parent.parent
                        }
                    }
                }
            }
        }
        return false
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
        // 1. Try finding EditText by class name
        val editTexts = mutableListOf<AccessibilityNodeInfo>()
        findNodesByClassName(rootNode, "android.widget.EditText", editTexts)
        if (editTexts.isNotEmpty()) {
            for (et in editTexts) {
                if (et.isEnabled && et.isFocusable) {
                    return et
                }
            }
            return editTexts.first()
        }

        // 2. Find any node where isEditable is true
        val editableNodes = mutableListOf<AccessibilityNodeInfo>()
        findEditableNodes(rootNode, editableNodes)
        if (editableNodes.isNotEmpty()) {
            return editableNodes.first()
        }

        // 3. Try finding by hint/text and navigating to editable container
        val hints = listOf("Nhập tin nhắn", "Tin nhắn", "Message", "Type a message")
        for (hint in hints) {
            val hintNodes = rootNode.findAccessibilityNodeInfosByText(hint)
            if (!hintNodes.isNullOrEmpty()) {
                for (node in hintNodes) {
                    var curr: AccessibilityNodeInfo? = node
                    while (curr != null) {
                        val isEditText = curr.className?.toString()?.contains("EditText") == true
                        if (curr.isEditable || isEditText) {
                            return curr
                        }
                        curr = curr.parent
                    }
                }
            }
        }

        return null
    }

    private fun findEditableNodes(
        root: AccessibilityNodeInfo,
        results: MutableList<AccessibilityNodeInfo>,
    ) {
        if (root.isEditable) {
            results.add(root)
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            findEditableNodes(child, results)
        }
    }

    private fun findAndClickSendButton(rootNode: AccessibilityNodeInfo): Boolean {
        // 1. First try resource IDs or content descriptions
        val sendButtonIds =
            listOf("btn_send", "chat_input_send_btn", "img_send", "send_btn", "btnSend", "send")
        val allNodes = mutableListOf<AccessibilityNodeInfo>()
        findAllNodes(rootNode, allNodes)

        for (node in allNodes) {
            val viewId = node.viewIdResourceName ?: ""
            val cd = node.contentDescription?.toString() ?: ""
            if (sendButtonIds.any { viewId.endsWith(it, ignoreCase = true) } ||
                cd.contains("gửi", ignoreCase = true) || cd.contains("send", ignoreCase = true)) {
                if (node.isClickable) {
                    Log.d(TAG, "Clicked send button by viewId/cd: $viewId / $cd")
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        Log.d(TAG, "Clicked send button parent by viewId/cd: $viewId / $cd")
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    parent = parent.parent
                }
            }
        }

        val sendIdentifiers = listOf("Gửi", "Send", "Gửi tin nhắn")
        for (identifier in sendIdentifiers) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(identifier)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    val text = node.text?.toString() ?: ""
                    val cd = node.contentDescription?.toString() ?: ""
                    if (text.equals(identifier, ignoreCase = true) || cd.equals(
                            identifier,
                            ignoreCase = true,
                        ) || cd.contains(identifier, ignoreCase = true)
                    ) {
                        if (node.isClickable) {
                            Log.d(TAG, "Clicked send button by text/cd: $identifier")
                            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                        var parent = node.parent
                        while (parent != null) {
                            if (parent.isClickable) {
                                Log.d(TAG, "Clicked send button parent by text/cd: $identifier")
                                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                            parent = parent.parent
                        }
                    }
                }
            }
        }

        // 2. Find input node, go to its parent container, and find the rightmost clickable ImageView/Button sibling or descendant
        val inputNode = findMessageInputNode(rootNode)
        if (inputNode != null) {
            var container: AccessibilityNodeInfo? = inputNode.parent
            while (container != null) {
                val clickableViews = mutableListOf<AccessibilityNodeInfo>()
                findClickableImageViewsOrButtons(container, clickableViews)
                // Filter out the input node itself
                clickableViews.removeIf { it == inputNode }

                if (clickableViews.isNotEmpty()) {
                    // Pick the rightmost clickable view (highest bounds.right or last)
                    val rightmost = clickableViews.maxByOrNull { view ->
                        val bounds = android.graphics.Rect()
                        view.getBoundsInScreen(bounds)
                        bounds.right
                    } ?: clickableViews.last()

                    if (rightmost.isClickable) {
                        Log.d(TAG, "Clicked rightmost send icon in input container")
                        return rightmost.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
                container = container.parent
                if (container == rootNode) break
            }
        }

        // 3. Fallback: Search all clickable ImageViews/Buttons in the window
        val imageViews = mutableListOf<AccessibilityNodeInfo>()
        findNodesByClassName(rootNode, "android.widget.ImageView", imageViews)
        val bottomClickableImageViews = imageViews.filter { iv -> iv.isClickable }
        if (bottomClickableImageViews.isNotEmpty()) {
            val rightmostBottom = bottomClickableImageViews.maxByOrNull { iv ->
                val bounds = android.graphics.Rect()
                iv.getBoundsInScreen(bounds)
                bounds.right
            }
            if (rightmostBottom?.isClickable == true) {
                Log.d(TAG, "Clicked rightmost ImageView as send button fallback")
                return rightmostBottom.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }

        return false
    }

    private fun findClickableImageViewsOrButtons(
        root: AccessibilityNodeInfo,
        results: MutableList<AccessibilityNodeInfo>,
    ) {
        val className = root.className?.toString() ?: ""
        if (root.isClickable && (className.contains("ImageView") || className.contains("Button") || className.contains(
                "TextView",
            ))
        ) {
            results.add(root)
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            findClickableImageViewsOrButtons(child, results)
        }
    }

    private fun findAllNodes(
        root: AccessibilityNodeInfo,
        results: MutableList<AccessibilityNodeInfo>
    ) {
        results.add(root)
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            findAllNodes(child, results)
        }
    }

    private fun findNodesByClassName(
        root: AccessibilityNodeInfo,
        targetClass: String,
        results: MutableList<AccessibilityNodeInfo>
    ) {
        if (root.className?.toString() == targetClass) {
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
        data class NotFound(val reason: String) : SendResult()
        data class Error(val reason: String) : SendResult()
    }

    companion object {
        private const val TAG = "ZaloAccessibility"
        private var instance: ZaloAccessibilityService? = null

        private val _isServiceActive = MutableStateFlow(value = false)

        @Suppress("unused")
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _sendEvents = MutableSharedFlow<SendResult>(extraBufferCapacity = 10)
        val sendEvents: SharedFlow<SendResult> = _sendEvents.asSharedFlow()

        private var isPendingActionActive = false
        private var currentPendingMessage: String? = null
        private var currentAutoAddFriend = false
        private var currentIsLowSpecDevice = false
        private var isActionInProgress = false
        private var isReturningToApp = false

        fun isAccessibilityEnabled(context: Context): Boolean {
            val am = context.getSystemService(AccessibilityManager::class.java)
                ?: return false
            val enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            val isEnabledInList = enabledServices.any { serviceInfo ->
                val pkgMatch = serviceInfo.resolveInfo.serviceInfo.packageName == context.packageName
                val nameMatch = serviceInfo.resolveInfo.serviceInfo.name == ZaloAccessibilityService::class.java.name
                pkgMatch && nameMatch
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

        fun prepareAutoSend(
            messageText: String,
            autoAddFriend: Boolean,
            isLowSpecDevice: Boolean = false
        ) {
            currentPendingMessage = messageText
            currentAutoAddFriend = autoAddFriend
            currentIsLowSpecDevice = isLowSpecDevice
            isPendingActionActive = true
            isActionInProgress = false
        }

        fun clearPendingAction() {
            isActionInProgress = false
            isPendingActionActive = false
            currentPendingMessage = null
        }

        fun cancelReturnToApp() {
            isReturningToApp = false
        }

        /**
         * Safely returns to AutoSms using AccessibilityService privileges:
         * 1. Attempts to bring MainActivity to front from AccessibilityService context (permitted in Android 10+).
         * 2. Runs a conditional BACK loop only while the foreground package is still Zalo,
         *    stopping immediately as soon as AutoSms is detected.
         */
        fun returnToAutoSms(context: Context, delayMs: Long = 400L) {
            val service = instance
            if (service == null) {
                Log.w(TAG, "returnToAutoSms: AccessibilityService instance is null! Falling back to context.startActivity")
                // Fallback to normal activity start if accessibility service is unavailable
                try {
                    val bringIntent = Intent(context, com.gmail.nthai.MainActivity::class.java).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                        )
                    }
                    context.startActivity(bringIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed fallback bring app to front", e)
                }
                return
            }

            Log.d(TAG, "returnToAutoSms: Initiating return sequence (delay: ${delayMs}ms)")
            isReturningToApp = true

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (!isReturningToApp) return@postDelayed

                // 1. First trigger GLOBAL_ACTION_BACK to exit conversation/input in Zalo
                service.performGlobalAction(GLOBAL_ACTION_BACK)

                // 2. Try launching MainActivity from AccessibilityService (bypasses Android 10 background restrictions)
                try {
                    val intent = Intent(service, com.gmail.nthai.MainActivity::class.java).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                        )
                    }
                    service.startActivity(intent)
                    Log.d(TAG, "Launched AutoSms MainActivity from AccessibilityService")
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting MainActivity from AccessibilityService", e)
                }

                // 3. Perform conditional back steps only while still on Zalo (max 3 attempts)
                scheduleConditionalBack(service, attempt = 1, maxAttempts = 3, intervalMs = 400L)
            }, delayMs)
        }

        private fun scheduleConditionalBack(
            service: ZaloAccessibilityService,
            attempt: Int,
            maxAttempts: Int,
            intervalMs: Long
        ) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (!isReturningToApp) {
                    Log.d(TAG, "Already back to AutoSms, stopping back sequence.")
                    return@postDelayed
                }

                val currentRoot = service.rootInActiveWindow
                val currentPkg = currentRoot?.packageName?.toString()

                if (currentPkg == service.packageName) {
                    Log.d(TAG, "AutoSms is now visible! Stopped back sequence.")
                    isReturningToApp = false
                    return@postDelayed
                }

                if (currentPkg == "com.zing.zalo" || currentPkg == null) {
                    Log.d(TAG, "Still on Zalo (pkg: $currentPkg). Triggering GLOBAL_ACTION_BACK (attempt $attempt/$maxAttempts)...")
                    service.performGlobalAction(GLOBAL_ACTION_BACK)

                    if (attempt < maxAttempts) {
                        scheduleConditionalBack(service, attempt + 1, maxAttempts, intervalMs)
                    } else {
                        isReturningToApp = false
                    }
                } else {
                    Log.d(TAG, "Active window is external package ($currentPkg), aborting back sequence.")
                    isReturningToApp = false
                }
            }, intervalMs)
        }

    }
}
