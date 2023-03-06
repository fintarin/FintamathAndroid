package com.fintamath.textview

import android.annotation.SuppressLint
import android.content.Context
import kotlin.jvm.JvmOverloads
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.util.AttributeSet
import android.view.View
import com.fintamath.R

class MathAlternativesTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val mathTextViewLayout: Int
    private val delimiterLayout: Int

    private var onTouchListener: OnTouchListener? = null

    private val inflate: LayoutInflater
    private val textViews = mutableListOf<MathTextView>()
    private val delimiters = mutableListOf<View>()

    init {
        orientation = VERTICAL

        val a = context.obtainStyledAttributes(attrs, R.styleable.MathAlternativesTextView)

        mathTextViewLayout =
            a.getResourceId(R.styleable.MathAlternativesTextView_alternativeTextViewLayout, 0)
        delimiterLayout =
            a.getResourceId(R.styleable.MathAlternativesTextView_alternativeDelimiterLayout, 0)

        a.recycle()

        inflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        addTextView()
    }

    fun setTexts(texts: List<String>) {
        for (i in textViews.size until texts.size) {
            addDelimiter()
            addTextView()
        }

        textViews[0].text = texts[0]

        val distinctTexts = texts.distinct()

        for (i in 1 until distinctTexts.size) {
            textViews[i].text = distinctTexts[i]
            textViews[i].visibility = VISIBLE
            delimiters[i - 1].visibility = VISIBLE
        }

        for (i in distinctTexts.size until textViews.size) {
            textViews[i].visibility = GONE
            textViews[i].clear()
            delimiters[i - 1].visibility = GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(listener: OnTouchListener) {
        onTouchListener = listener

        for (text in textViews) {
            text.setOnTouchListener(onTouchListener)
        }

        super.setOnTouchListener(onTouchListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addTextView() {
        val textView = inflate.inflate(mathTextViewLayout, null) as MathTextView
        textView.setOnTouchListener(onTouchListener)
        textViews.add(textView)
        addView(textView)
    }

    private fun addDelimiter() {
        val delimiter = inflate.inflate(delimiterLayout, null)
        delimiter.visibility = GONE
        delimiter.setOnTouchListener(onTouchListener)
        delimiters.add(delimiter)
        addView(delimiter)
    }
}
