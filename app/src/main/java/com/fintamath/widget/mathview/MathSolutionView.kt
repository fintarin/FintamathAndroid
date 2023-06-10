package com.fintamath.widget.mathview

import android.content.Context
import kotlin.jvm.JvmOverloads
import android.view.LayoutInflater
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.fintamath.R

class MathSolutionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private val loadingViewLayout: Int
    private val invalidInputViewLayout: Int
    private val incompleteInputViewLayout: Int
    private val characterLimitExceededViewLayout: Int

    private val alternativesView: MathSolutionAlternativesView
    private val loadingView: View
    private val invalidInputView: View
    private val incompleteInputView: View
    private val characterLimitExceededView: View

    private var currentView: View? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MathSolutionView)

        loadingViewLayout =
            a.getResourceId(R.styleable.MathSolutionView_loadingViewLayout, 0)
        invalidInputViewLayout =
            a.getResourceId(R.styleable.MathSolutionView_invalidInputViewLayout, 0)
        incompleteInputViewLayout =
            a.getResourceId(R.styleable.MathSolutionView_incompleteInputViewLayout, 0)
        characterLimitExceededViewLayout =
            a.getResourceId(R.styleable.MathSolutionView_characterLimitExceededViewLayout, 0)

        alternativesView = MathSolutionAlternativesView(context, a)
        alternativesView.visibility = GONE
        addView(alternativesView)

        a.recycle()

        loadingView = inflate(context, loadingViewLayout, null)
        loadingView.visibility = GONE
        addView(loadingView)

        invalidInputView = inflate(context, invalidInputViewLayout, null)
        invalidInputView.visibility = GONE
        addView(invalidInputView)

        incompleteInputView = inflate(context, incompleteInputViewLayout, null)
        incompleteInputView.visibility = GONE
        addView(incompleteInputView)

        characterLimitExceededView = inflate(context, characterLimitExceededViewLayout, null)
        characterLimitExceededView.visibility = GONE
        addView(characterLimitExceededView)
    }

    fun showSolution(texts: List<String>) {
        alternativesView.setTexts(texts)
        showView(alternativesView)
    }

    fun showLoading() {
        showView(loadingView)
    }

    fun showInvalidInput() {
        showView(invalidInputView)
    }

    fun showIncompleteInput() {
        showView(incompleteInputView)
    }

    fun showCharacterLimitExceeded() {
        showView(characterLimitExceededView)
    }

    fun hideCurrentView() {
        alternativesView.setTexts(listOf(""))

        if (currentView != null) {
            currentView!!.visibility = GONE
        }

        currentView = null
    }

    private fun showView(view: View) {
        if (currentView != null && view != currentView) {
            currentView!!.visibility = GONE
        }

        currentView = view
        currentView!!.visibility = VISIBLE
    }
}
