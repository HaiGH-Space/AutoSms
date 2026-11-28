package com.gmail.nthai.domain.telephony

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class DeviceCallState {
    IDLE,
    RINGING,
    OFFHOOK
}

@Singleton
class PhoneCallStateObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val _callStateFlow = MutableStateFlow(DeviceCallState.IDLE)
    val callStateFlow: StateFlow<DeviceCallState> = _callStateFlow.asStateFlow()

    val currentState: DeviceCallState
        get() = _callStateFlow.value

    private var legacyListener: PhoneStateListener? = null
    private var modernCallback: TelephonyCallback? = null
    private var isListening = false

    fun startListening() {
        if (isListening || telephonyManager == null) return
        isListening = true
        _callStateFlow.value = DeviceCallState.IDLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    emitState(state)
                }
            }
            modernCallback = callback
            try {
                telephonyManager.registerTelephonyCallback(
                    context.mainExecutor,
                    callback
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    emitState(state)
                }
            }
            legacyListener = listener
            try {
                @Suppress("DEPRECATION")
                telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun stopListening() {
        if (!isListening || telephonyManager == null) return
        isListening = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            modernCallback?.let {
                try {
                    telephonyManager.unregisterTelephonyCallback(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            modernCallback = null
        } else {
            legacyListener?.let {
                try {
                    @Suppress("DEPRECATION")
                    telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            legacyListener = null
        }
    }

    private fun emitState(telephonyState: Int) {
        val mappedState = when (telephonyState) {
            TelephonyManager.CALL_STATE_RINGING -> DeviceCallState.RINGING
            TelephonyManager.CALL_STATE_OFFHOOK -> DeviceCallState.OFFHOOK
            else -> DeviceCallState.IDLE
        }
        _callStateFlow.value = mappedState
    }
}
