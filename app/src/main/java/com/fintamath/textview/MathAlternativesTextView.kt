package com.fintamath.textview

import android.content.Context
import kotlin.jvm.JvmOverloads
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.widget.TextView
import android.util.AttributeSet
import com.fintamath.R
import android.view.ViewGroup

class MathAlternativesTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val mTextViewLayout: Int
    private val mDelimiterLayout: Int
    private val mLayout: Int

    private val mInflate: LayoutInflater
    private val mMainTextView: TextView

    init {
        orientation = VERTICAL

        val a = context.obtainStyledAttributes(attrs, R.styleable.MathAlternativesTextView)

        mLayout = a.getResourceId(R.styleable.MathAlternativesTextView_alternativeLayout, 0)
        mTextViewLayout =
            a.getResourceId(R.styleable.MathAlternativesTextView_alternativeTextViewLayout, 0)
        mDelimiterLayout =
            a.getResourceId(R.styleable.MathAlternativesTextView_alternativeDelimiterLayout, 0)

        a.recycle()

        mInflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        mMainTextView = mInflate.inflate(mTextViewLayout, null) as TextView
        addTextView(mMainTextView)
    }

    fun setTexts(texts: List<String>?) {
        removeViews(1, childCount - 1)

        if (texts == null || texts.isEmpty()) {
            mMainTextView.text = ""
            return
        }

        mMainTextView.text = texts[0]

        for (i in 1 until texts.size) {
            if (!isTextUnique(texts[i])) {
                continue
            }

            val alternativeTextView = mInflate.inflate(mTextViewLayout, null) as TextView
            addView(mInflate.inflate(mDelimiterLayout, null))
            addTextView(alternativeTextView)
            alternativeTextView.text = texts[i]
        }
    }

    private fun addTextView(textView: TextView) {
        val scrollView = mInflate.inflate(mLayout, null) as ViewGroup
        scrollView.foregroundGravity = foregroundGravity
        scrollView.addView(textView)
        addView(scrollView)
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
}
