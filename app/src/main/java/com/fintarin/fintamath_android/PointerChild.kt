package com.fintarin.fintamath_android

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import java.util.*

class PointerChild:ChildExpression {
    private lateinit var parent:ParentExpression
    private lateinit var layout:ViewGroup
    private lateinit var textView: TextView
    private var isSelected:Boolean=false
    override fun selfDestruct() {
        layout.removeAllViews()
        parent.getLayout().removeView(layout)
    }

    override fun setParent(parent: ParentExpression) {
        this.parent=parent
        layout = LinearLayout(parent.getLayout().context)
        layout.layoutParams=LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        layout.setOnClickListener {
            this.setSelected()
        }
        textView= TextView(layout.context)
        textView.layoutParams=LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        textView.setTextSize(40.0F)
        textView.setTextColor(Color.GRAY)
        textView.setBackgroundColor(Color.GRAY)
        textView.text=("?")
        layout.addView(textView)
    }
    override fun getText(): String {
        return "?"
    }

    override fun isSelected(): Boolean {
        return isSelected
    }

    override fun setSelected() {
        isSelected=true
    }

    override fun getLayout(): ViewGroup {
        return layout
    }

    override fun setUnselected() {
        isSelected=false
    }

}