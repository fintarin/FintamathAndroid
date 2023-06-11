package com.fintamath.widget.mathview

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.PopupWindow
import androidx.annotation.Keep
import com.fintamath.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.math.abs

@Keep
class MathTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : WebView(context, attrs) {

    private val initHtml = "file:///android_asset/math_text_view/math_text_view.html"

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

    private val quickActionPopupLayout: Int

    private var prevX = 0f
    private var prevY = 0f
    private var wasLastScrollHorizontal = false

    private var onTextChangedListener: ((text: String) -> Unit)? = null

    private var isLoaded = false
    private var onLoadedCallbacks: MutableList<(() -> Unit)> = mutableListOf()

    private val uiHandler: Handler = Handler(Looper.getMainLooper())

    private val quickActionPopup: PopupWindow
    private val cutActionButton: Button
    private val copyActionButton: Button
    private val pasteActionButton: Button
    private val deleteActionButton: Button

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

    private val gestureDetector = GestureDetector(context, object :
        GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(event: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(event: MotionEvent) {
            this@MathTextView.onLongPress(event)
        }
    })

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
        quickActionPopupLayout = a.getResourceId(R.styleable.MathTextView_quickActionPopupLayout, 0)

        a.recycle()

        quickActionPopup = PopupWindow(context)
        quickActionPopup.isFocusable = true
        quickActionPopup.contentView = inflate(context, quickActionPopupLayout, null)
        quickActionPopup.setBackgroundDrawable(null)

        cutActionButton = quickActionPopup.contentView.findViewById(R.id.cut)
        cutActionButton.setOnClickListener { onCutAction() }

        copyActionButton = quickActionPopup.contentView.findViewById(R.id.copy)
        copyActionButton.setOnClickListener { onCopyAction() }

        pasteActionButton = quickActionPopup.contentView.findViewById(R.id.paste)
        pasteActionButton.setOnClickListener { onPasteAction() }

        deleteActionButton = quickActionPopup.contentView.findViewById(R.id.delete)
        deleteActionButton.setOnClickListener { onDeleteAction() }

        setOnLongClickListener { return@setOnLongClickListener true }
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

    private fun onCutAction() {
        onCopyAction()
        clear()
    }

    private fun onCopyAction() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("math-text", text)
        clipboard.setPrimaryClip(clip)

        quickActionPopup.dismiss()
    }

    private fun onPasteAction() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        insertAtCursor(clipboard.primaryClip?.getItemAt(0)?.text.toString())

        quickActionPopup.dismiss()
    }

    private fun onDeleteAction() {
        clear()

        quickActionPopup.dismiss()
    }

    fun setOnTextChangedListener(listener: (text: String) -> Unit) {
        onTextChangedListener = listener
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (!isLoaded) {
            onLoaded {
                requestFocus(direction, previouslyFocusedRect)
            }
        }

        return super.requestFocus(direction, previouslyFocusedRect)
    }

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

        if (gestureDetector.onTouchEvent(event)) {
            return true
        }

        return super.onTouchEvent(event)
    }

    private fun onLongPress(event: MotionEvent) {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

        cutActionButton.visibility = if (isEditable) VISIBLE else GONE
        pasteActionButton.visibility = if (isEditable) VISIBLE else GONE
        deleteActionButton.visibility = if (isEditable) VISIBLE else GONE

        val location = IntArray(2)
        getLocationOnScreen(location)

        quickActionPopup.contentView.measure(0, 0)
        quickActionPopup.showAtLocation(this, Gravity.NO_GRAVITY,
            event.x.toInt() - quickActionPopup.contentView.measuredWidth / 2,
            event.y.toInt() + location[1] - quickActionPopup.contentView.measuredHeight * 3/2)

        GlobalScope.launch {
            delay(50)

            dispatchTouchEvent(MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                event.x,
                event.y,
                0
            ))
            dispatchTouchEvent(MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                event.x,
                event.y,
                0
            ))
        }
    }

    override fun evaluateJavascript(script: String, resultCallback: ValueCallback<String>?) {
        onLoaded {
            super.evaluateJavascript(script, resultCallback)
        }
    }

    @JavascriptInterface
    fun onTextChange(newText: String, isCompleteStr: String) {
        textCached = newText
        isComplete = isCompleteStr == "true"

        getActivity()?.runOnUiThread {
            onTextChangedListener?.invoke(newText)
        }
    }

    private fun onLoaded(callback: () -> Unit) {
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
