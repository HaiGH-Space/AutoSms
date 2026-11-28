package com.gmail.nthai.domain.telephony

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallOverlayManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private var windowManager: WindowManager? = null
    private var overlayView: LinearLayout? = null
    private var nameTextView: TextView? = null
    private var phoneTextView: TextView? = null

    private var lastX: Int? = null
    private var lastY: Int? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    @Suppress("DEPRECATION")
    fun showOverlay(name: String, phone: String) {
        if (!canDrawOverlays()) return

        try {
            if (windowManager == null) {
                windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            }

            if (overlayView == null) {
                createOverlayView()
            }

            nameTextView?.text = name.ifBlank { phone }
            phoneTextView?.text = "SĐT: $phone"

            if ((overlayView?.parent == null) && (windowManager != null)) {
                val density = context.resources.displayMetrics.density
                val screenWidth = context.resources.displayMetrics.widthPixels
                val defaultX = (screenWidth - (220 * density).toInt()).coerceAtLeast(0) / 2
                val defaultY = (100 * density).toInt()

                val layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else
                        WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    PixelFormat.TRANSLUCENT,
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = lastX ?: defaultX
                    y = lastY ?: defaultY
                }

                windowManager?.addView(overlayView, layoutParams)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideOverlay() {
        try {
            if ((overlayView != null) && (overlayView?.parent != null)) {
                windowManager?.removeView(overlayView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createOverlayView() {
        val density = context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padH = (18 * density).toInt()
            val padV = (10 * density).toInt()
            setPadding(padH, padV, padH, padV)

            val backgroundDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#171717"))
                cornerRadius = 16 * density
                setStroke((1 * density).toInt(), Color.parseColor("#333333"))
            }
            background = backgroundDrawable
            elevation = 10 * density
        }

        // Top drag handle bar
        val dragHandle = View(context).apply {
            val barWidth = (36 * density).toInt()
            val barHeight = (4 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(barWidth, barHeight).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = (6 * density).toInt()
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#555555"))
                cornerRadius = 2 * density
            }
        }

        val tagTextView = TextView(context).apply {
            text = "🟢 Auto Call - Đang gọi (Kéo để di chuyển)"
            setTextColor(Color.parseColor("#0070F3"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val nameTv = TextView(context).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val phoneTv = TextView(context).apply {
            setTextColor(Color.parseColor("#AAAAAA"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        container.addView(dragHandle)
        container.addView(tagTextView)
        container.addView(nameTv)
        container.addView(phoneTv)

        // Drag to move overlay across screen
        container.setOnTouchListener { _, event ->
            val lp = container.layoutParams as? WindowManager.LayoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = lp.x
                    initialY = lp.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = initialX + (event.rawX - initialTouchX).toInt()
                    val newY = initialY + (event.rawY - initialTouchY).toInt()
                    lp.x = newX
                    lp.y = newY
                    lastX = newX
                    lastY = newY
                    try {
                        windowManager?.updateViewLayout(container, lp)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    true
                }
                else -> false
            }
        }

        nameTextView = nameTv
        phoneTextView = phoneTv
        overlayView = container
    }
}
