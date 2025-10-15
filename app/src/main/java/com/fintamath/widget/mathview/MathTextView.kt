package com.fintamath.widget.mathview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.PopupWindow
import androidx.annotation.Keep
import com.fintamath.R
import com.fintamath.utils.getActivity
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
            if (value != text) {
                evaluateJavascript("setText(\"$value\")") { }
            }
        }

    var textColor: Int = 0xFF000000.toInt()
        set(value) {
            if (field != value) {
                field = value
                evaluateJavascript("setColor(\"${toHexString(field)}\")") { }
            }
        }

    var textSize: Int = 18
        set(value) {
            if (field != value) {
                field = value
                settings.defaultFontSize = textSize
            }
        }

    var isEditable: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                evaluateJavascript("setContentEditable(\"$field\")") { }
            }
        }

    var isComplete = true
        private set

    private var textCached = ""
    private var visibilityCached = VISIBLE

    private val quickActionPopupLayout: Int

    private var prevX = 0f
    private var prevY = 0f
    private var isClicking = false
    private var wasLastScrollHorizontal = false

    private var onTextChangedListener: ((textView: MathTextView, text: String) -> Unit)? = null
    private var onClickListener: OnClickListener? = null

    private var isLoaded = false
    private var onLoadedCallbacks: MutableList<(() -> Unit)> = mutableListOf()

    private var quickActionPopup: PopupWindow? = null
    private var cutActionButton: Button? = null
    private var copyActionButton: Button? = null
    private var pasteActionButton: Button? = null
    private var deleteActionButton: Button? = null

    private val mathTextViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            visibility = visibilityCached
            isLoaded = true

            for (callback in onLoadedCallbacks) {
                callback.invoke()
            }
        }
    }

    init {
        val initialVisibility = visibility
        visibility = INVISIBLE
        visibilityCached = initialVisibility

        scrollBarSize = 0
        setBackgroundColor(Color.TRANSPARENT)

        webViewClient = mathTextViewClient
        settings.javaScriptEnabled = true
        addJavascriptInterface(this, "Android")

        loadUrl(initHtml)

        val a = context.obtainStyledAttributes(attrs, R.styleable.MathTextView)

        textColor = a.getColor(R.styleable.MathTextView_textColor, textColor)
        textSize = toSp(a.getDimensionPixelSize(R.styleable.MathTextView_textSize, toPx(textSize)))
        isEditable = a.getBoolean(R.styleable.MathTextView_isEditable, isEditable)
        text = a.getString(R.styleable.MathTextView_text) ?: textCached
        quickActionPopupLayout = a.getResourceId(R.styleable.MathTextView_quickActionPopupLayout, 0)

        a.recycle()

        if (quickActionPopupLayout != 0) {
            initQuickActionPopup(context)
        }

        setOnLongClickListener { onLongClick() }
    }

    private fun initQuickActionPopup(context: Context) {
        quickActionPopup = PopupWindow(context)
        quickActionPopup!!.isFocusable = true
        quickActionPopup!!.contentView = inflate(context, quickActionPopupLayout, null)
        quickActionPopup!!.animationStyle = android.R.style.Animation_Dialog
        quickActionPopup!!.setBackgroundDrawable(null)

        cutActionButton = quickActionPopup!!.contentView.findViewById(R.id.cut)
        cutActionButton!!.setOnClickListener {
            onCut()
            quickActionPopup!!.dismiss()
        }

        copyActionButton = quickActionPopup!!.contentView.findViewById(R.id.copy)
        copyActionButton!!.setOnClickListener {
            onCopy()
            quickActionPopup!!.dismiss()
        }

        pasteActionButton = quickActionPopup!!.contentView.findViewById(R.id.paste)
        pasteActionButton!!.setOnClickListener {
            onPaste()
            quickActionPopup!!.dismiss()
        }

        deleteActionButton = quickActionPopup!!.contentView.findViewById(R.id.delete)
        deleteActionButton!!.setOnClickListener {
            clear()
            quickActionPopup!!.dismiss()
        }
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

    fun clearUndoStates() {
        evaluateJavascript("clearUndoStates()") { }
    }

    fun moveCursorLeft() {
        evaluateJavascript("moveCursorLeft()") { }
    }

    fun moveCursorRight() {
        evaluateJavascript("moveCursorRight()") { }
    }

    fun setOnTextChangedListener(listener: ((textView: MathTextView, text: String) -> Unit)?) {
        onTextChangedListener = listener
    }

    fun requestFocusInWeb() {
        if (hasFocus()) {
            return
        }

        if (!isLoaded) {
            onLoaded {
                requestFocusInWeb()
            }
        }
        else {
            super.requestFocus()
            evaluateJavascript("requestFocus()") { }
        }
    }

    fun clearFocusInWeb() {
        if (!hasFocus()) {
            return
        }

        if (!isLoaded) {
            onLoaded {
                clearFocusInWeb()
            }
        }
        else {
            super.clearFocus()
            evaluateJavascript("clearFocus()") { }
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection {
        return BaseInputConnection(this, false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isClicking = true
            }
            MotionEvent.ACTION_UP -> {
                if (isClicking && onClickListener != null) {
                    onClickListener!!.onClick(this)
                }

                isClicking = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.x - prevX) > abs(event.y - prevY)) {
                    wasLastScrollHorizontal = true
                    requestDisallowInterceptTouchEvent(true)
                }

                val delta = 0.01;
                if (abs(event.x - prevX) > delta || abs(event.y - prevY) > delta) {
                    isClicking = false
                }
            }
            else -> {
                isClicking = false
            }
        }

        prevX = event.x
        prevY = event.y

        return super.onTouchEvent(event)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        visibilityCached = visibility
    }

    private fun onLongClick(): Boolean {
        isClicking = false

        if (!isEditable) {
            onCopy()
            return true
        }

        if (quickActionPopup == null) {
            return true
        }

        cutActionButton!!.visibility = if (text.isNotEmpty()) VISIBLE else GONE
        deleteActionButton!!.visibility = if (text.isNotEmpty()) VISIBLE else GONE
        copyActionButton!!.visibility = if (text.isNotEmpty()) VISIBLE else GONE

        val location = IntArray(2)
        getLocationOnScreen(location)

        quickActionPopup!!.contentView.measure(0, 0)
        quickActionPopup!!.showAtLocation(this, Gravity.NO_GRAVITY,
            prevX.toInt() - quickActionPopup!!.contentView.measuredWidth / 2,
            prevY.toInt() + location[1] - quickActionPopup!!.contentView.measuredHeight * 3/2)

        dispatchTouchEvent(MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_UP,
            prevX,
            prevY,
            0
        ))
        dispatchTouchEvent(MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_DOWN,
            prevX,
            prevY,
            0
        ))
        dispatchTouchEvent(MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_UP,
            prevX,
            prevY,
            0
        ))

        return true
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        onClickListener = listener
    }

    override fun evaluateJavascript(script: String, resultCallback: ValueCallback<String>?) {
        onLoaded {
            super.evaluateJavascript(script, resultCallback)
        }
    }

    private fun onLoaded(callback: () -> Unit) {
        if (isLoaded) {
            callback.invoke()
        } else {
            onLoadedCallbacks.add(callback)
        }
    }

    @JavascriptInterface
    fun onTextChange(newText: String, isCompleteStr: String) {
        textCached = newText
        isComplete = isCompleteStr == "true"

        getActivity(context)?.runOnUiThread {
            onTextChangedListener?.invoke(this, newText)
        }
    }

    @JavascriptInterface
    fun onCut() {
        onCopy()
        clear()
    }

    @JavascriptInterface
    fun onCopy() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("math-text", text)
        clipboard.setPrimaryClip(clip)
    }

    @JavascriptInterface
    fun onPaste() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val pasteText = clipboard.primaryClip?.getItemAt(0)?.text

        if (pasteText != null) {
            insertAtCursor(pasteText.toString().replace("[\n\r]".toRegex(), " "))
        }
    }

    private fun toHexString(color: Int): String {
        return '#' + Integer.toHexString(color).substring(2)
    }

    @Suppress("DEPRECATION")
    private fun toSp(size: Int): Int {
        return (size  / resources.displayMetrics.scaledDensity).toInt()
    }

    @Suppress("DEPRECATION")
    private fun toPx(size: Int): Int {
        return (size  * resources.displayMetrics.scaledDensity).toInt()
    }
}
