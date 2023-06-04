package com.fintamath.widget.mathview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import com.fintamath.R
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.math.abs

@Keep
class MathTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : WebView(context, attrs) {

    var text: String
        get() = textCached
        set(value) {
            evaluateJavascript("setText(\"$value\")") { }
        }

    var hint: String = ""
        set(value) {
            field = value
            evaluateJavascript("setHint(\"$field\")") { }
        }

    var textColor: Int = 0xFF000000.toInt()
        set(value) {
            field = value
            evaluateJavascript("setColor(\"${toHexString(field)}\")") { }
        }

    var textSize: Int = 18
        set(value) {
            field = value
            settings.defaultFontSize = textSize
        }

    var isEditable: Boolean = false
        set(value) {
            field = value
            evaluateJavascript("setContentEditable(\"$field\")") { }
        }

    var isComplete = true
        private set

    private var textCached = ""
    private var prevX = 0f
    private var prevY = 0f
    private var wasLastScrollHorizontal = false

    private var onTextChangedListener: ((text: String) -> Unit)? = null

    private var isLoaded = false
    private var onLoadedCallbacks: MutableList<(() -> Unit)> = mutableListOf()

    private val uiHandler: Handler = Handler(Looper.getMainLooper())

    private val mathTextViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            Timer().schedule(10) {
                uiHandler.post {
                    isLoaded = true
                    visibility = VISIBLE

                    for (callback in onLoadedCallbacks) {
                        callback.invoke()
                    }
                }
            }
        }
    }

    private val initHtml = "file:///android_asset/math_text_view/math_text_view.html"

    init {
        visibility = INVISIBLE
        scrollBarSize = 0
        setBackgroundColor(Color.TRANSPARENT)

        settings.javaScriptEnabled = true
        addJavascriptInterface(this, "Android")
        loadUrl(initHtml)
        webViewClient = mathTextViewClient

        val a = context.obtainStyledAttributes(attrs, R.styleable.MathTextView)

        hint = a.getString(R.styleable.MathTextView_hint) ?: hint
        textColor = a.getColor(R.styleable.MathTextView_textColor, textColor)
        textSize = toSp(a.getDimensionPixelSize(R.styleable.MathTextView_textSize, toPx(textSize)))
        isEditable = a.getBoolean(R.styleable.MathTextView_isEditable, isEditable)
        text = a.getString(R.styleable.MathTextView_text) ?: textCached

        a.recycle()
    }

    fun insertAtCursor(text: String) {
        evaluateJavascript("insertAtCursor(\"$text\")") { }
    }

    fun deleteAtCursor() {
        evaluateJavascript("deleteAtCursor()") { }
    }

    fun clear() {
        evaluateJavascript("clear()") { }
    }

    fun undo() {
        evaluateJavascript("undo()") { }
    }

    fun redo() {
        evaluateJavascript("redo()") { }
    }

    fun moveCursorLeft() {
        evaluateJavascript("moveCursorLeft()") { }
    }

    fun moveCursorRight() {
        evaluateJavascript("moveCursorRight()") { }
    }

    fun setOnTextChangedListener(listener: (text: String) -> Unit) {
        onTextChangedListener = listener
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (!isLoaded) {
            callOnLoaded {
                requestFocus(direction, previouslyFocusedRect)
            }
        }

        return super.requestFocus(direction, previouslyFocusedRect)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.x - prevX) > abs(event.y - prevY)) {
                    wasLastScrollHorizontal = true
                    requestDisallowInterceptTouchEvent(true)
                }
            }
        }

        prevX = event.x
        prevY = event.y

        return super.onTouchEvent(event)
    }

    override fun evaluateJavascript(script: String, resultCallback: ValueCallback<String>?) {
        callOnLoaded {
            super.evaluateJavascript(script, resultCallback)
        }
    }

    @JavascriptInterface
    fun callOnTextChanged(newText: String, isCompleteStr: String) {
        textCached = newText
        isComplete = isCompleteStr == "true"

        getActivity()?.runOnUiThread {
            onTextChangedListener?.invoke(newText)
        }
    }

    private fun callOnLoaded(callback: () -> Unit) {
        if (isLoaded) {
            callback.invoke()
        } else {
            onLoadedCallbacks.add(callback)
        }
    }

    private fun toHexString(color: Int): String {
        return '#' + Integer.toHexString(color).substring(2)
    }

    private fun toSp(size: Int): Int {
        return (size  / resources.displayMetrics.scaledDensity).toInt()
    }

    private fun toPx(size: Int): Int {
        return (size  * resources.displayMetrics.scaledDensity).toInt()
    }

    private fun getActivity(): Activity? {
        var activityContext: Context? = context

        while (activityContext is ContextWrapper) {
            if (activityContext is Activity) {
                return activityContext
            }

            activityContext = activityContext.baseContext
        }

        return null
    }
}
