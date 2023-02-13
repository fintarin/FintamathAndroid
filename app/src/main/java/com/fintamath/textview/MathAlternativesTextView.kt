package com.fintamath.textview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlin.jvm.JvmOverloads
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.widget.TextView
import android.util.AttributeSet
import android.view.View
import com.fintamath.R
import android.view.ViewGroup
import android.widget.HorizontalScrollView

class MathAlternativesTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val textViewLayout: Int
    private val delimiterLayout: Int

    private val inflate: LayoutInflater

    private val alternativeTextViews = mutableListOf<TextView>()
    private val alternativeDelimiters = mutableListOf<View>()

    init {
        orientation = VERTICAL

        val a = context.obtainStyledAttributes(attrs, R.styleable.MathAlternativesTextView)

        textViewLayout =
            a.getResourceId(R.styleable.MathAlternativesTextView_alternativeTextViewLayout, 0)
        delimiterLayout =
            a.getResourceId(R.styleable.MathAlternativesTextView_alternativeDelimiterLayout, 0)

        a.recycle()

        inflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        addTextView()
    }

    fun setTexts(texts: List<String>?) {
        if (texts == null) {
            setTexts(listOf(""))
            return
        }

        for (i in alternativeTextViews.size until texts.size) {
            addDelimiter()
            addTextView()
        }

        alternativeTextViews[0].text = texts[0]
        alternativeTextViews[0].isLongClickable = alternativeTextViews[0].text.isNotEmpty()
        alternativeTextViews[0].requestLayout()

        var uniqueTextsSize = 1

        for (i in 1 until texts.size) {
            if (!isTextUnique(texts[i])) {
                continue
            }

            alternativeTextViews[uniqueTextsSize].text = texts[i]
            alternativeTextViews[uniqueTextsSize].isLongClickable = true
            alternativeTextViews[uniqueTextsSize].requestLayout()
            alternativeDelimiters[uniqueTextsSize - 1].visibility = VISIBLE

            uniqueTextsSize++
        }

        for (i in uniqueTextsSize until alternativeTextViews.size) {
            alternativeTextViews[i].text = ""
            alternativeTextViews[i].isLongClickable = false
            alternativeDelimiters[i - 1].visibility = GONE
        }
    }

    private fun addTextView() {
        val textView = inflate.inflate(textViewLayout, null) as TextView
        textView.isLongClickable = false
        textView.setOnLongClickListener(textViewOnLongClick())

        alternativeTextViews.add(textView)

        val scrollView = HorizontalScrollView(context)
        scrollView.foregroundGravity = foregroundGravity
        scrollView.isHorizontalScrollBarEnabled = false
        scrollView.addView(textView)

        addView(scrollView)
    }

    private fun addDelimiter() {
        val delimiter = inflate.inflate(delimiterLayout, null)
        delimiter.visibility = GONE

        alternativeDelimiters.add(delimiter)

        addView(delimiter)
    }

    private fun isTextUnique(text: String): Boolean {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view is ViewGroup) {
                val textView = view.getChildAt(0) as TextView
                if (textView.text.toString() == text) {
                    return false
                }
            }
        }

        return true
    }

    private fun textViewOnLongClick(): (v: View) -> Boolean = {
        val clipboardManager = context.getSystemService(ClipboardManager::class.java)
        val clipData = ClipData.newPlainText("", (it as TextView).text)
        clipboardManager.setPrimaryClip(clipData)
        true
    }
}
