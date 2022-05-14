package com.fintarin.fintamath_android

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class Symbol(_symbol: String):ChildExpression {
    private var isSelected:Boolean=false
    private var symbol:String=_symbol
    private lateinit var layout: ViewGroup
    private lateinit var parent:Expression
    private lateinit var textView: TextView
    override fun setParent(parent: ParentExpression) {
        this.parent=parent
        layout = LinearLayout(parent.getLayout().context)
        layout.layoutParams=LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        layout.setOnClickListener {
            parent.setDescendantUnselected()
            this.setSelected()
            parent.addPointerChild()
        }
        textView= TextView(layout.context)
        textView.layoutParams=LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        textView.setTextSize(40.0F)
        textView.setTextColor(Color.BLACK)
        textView.text=(symbol)
        layout.addView(textView)
    }

    override fun selfDestruct() {
        this.layout.removeAllViews()
        parent.getLayout().removeView(this.layout)
    }

    override fun getText(): String {
        return symbol
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