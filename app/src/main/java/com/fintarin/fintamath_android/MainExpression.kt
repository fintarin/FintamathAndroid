package com.fintarin.fintamath_android

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.marginLeft
import com.fintarin.fintamath_android.databinding.ActivityMainBinding
import java.util.*

class MainExpression(_parentLayout: ViewGroup,binding: ActivityMainBinding): ParentExpression {
    private var parentLayout:ViewGroup = _parentLayout
    private var layout:ViewGroup = LinearLayout(parentLayout.context)
    private var children:MutableList<ChildExpression> = LinkedList<ChildExpression>()
    private var isSelected:Boolean=true
    private var hasSelectedDescendant:Boolean=true
    init {
        layout.layoutParams=LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
        layout.setPadding(30,0,0,0)
        parentLayout.addView(layout)
        layout.setOnClickListener {
            if(!this.isSelected) {
                this.setSelected()
            }
        }
        addPointerChild(0)
    }

    override fun getText(): String {
        var expression:String=""
        for(child in children){
            expression+=child.getText()
        }
        expression=expression.replace("?","")
        return expression
    }

    override fun isSelected():Boolean {
        return isSelected
    }
    override fun hasSelectedDescendant():Boolean{
        return hasSelectedDescendant
    }

    override fun addChild(childExpression: ChildExpression) {
        if (isSelected()) {
            childExpression.setParent(this)
            children.add(0,childExpression)
            layout.addView(childExpression.getLayout(),0)
            isSelected=false
            childExpression.setSelected()
        }
        else {
            var selectedChild:ChildExpression?=null
            for (child in children) {
                if (child.isSelected()) {
                    selectedChild=child
                }
            }
            if(selectedChild!=null) {
                addChild(childExpression, selectedChild)
            }
            else{
                for (child in children){
                    if(child is ParentExpression && child.hasSelectedDescendant()){
                        child.addChild(childExpression)
                    }
                }
            }
        }
    }

    override fun addChild(childExpression: ChildExpression, child: ChildExpression) {
        deletePointerChild()
        childExpression.setParent(this)
        this.isSelected=false
        children.add(children.indexOf(child)+1,childExpression)
        layout.addView(childExpression.getLayout(),children.indexOf(child)+1)
        childExpression.setSelected()
    }

    override fun addPointerChild() {
        var selectedChildPosition:Int=0
        for(child in children) {
            if (child.isSelected())
            {
                selectedChildPosition=children.indexOf(child)
            }
        }
            val pointerChild = PointerChild()
            pointerChild.setParent(this)
            children.add(selectedChildPosition+1, pointerChild)
            layout.addView(pointerChild.getLayout(), selectedChildPosition+1)
    }

    override fun addPointerChild(position:Int) {
        val pointerChild=PointerChild()
        pointerChild.setParent(this)
        children.add(position,pointerChild)
        layout.addView(pointerChild.getLayout(),position)
    }

    override fun deletePointerChild() {
        var pointerChild:ChildExpression?=null
        for (child in children){
            if (child.getText() == "?"){
                pointerChild=child
            }
        }
        if(pointerChild!=null) {
            layout.removeView(pointerChild.getLayout())
            children.remove(pointerChild)
        }
    }

    override fun deleteChild() {
        var childToDelete:ChildExpression?=null
        for (child in children){
            if (child.isSelected()){
                childToDelete=child
            }
            else if(child is ParentExpression && child.hasSelectedDescendant()){
                child.deleteChild()
            }
        }
        if (childToDelete != null)  {
            Log.d("DJ-Tape","Delete child${childToDelete.getText()}")
            if(children.indexOf(childToDelete)-1>=0){
            children[children.indexOf(childToDelete)-1].setSelected()
                }
            else{
                this.setSelected()
            }
            childToDelete.selfDestruct()
            children.remove(childToDelete)
            }
        }

    override fun setSelected() {
        deletePointerChild()
        setDescendantUnselected()
        addPointerChild()
        isSelected=true
    }

    override fun getLayout():ViewGroup {
        return layout
    }

    override fun setUnselected() {
        deletePointerChild()
        isSelected=false
    }

    override fun setDescendantUnselected() {
        deletePointerChild()
        for (child in children){
            if(child.isSelected()){
                child.setUnselected()
            }
            else if(child is ParentExpression){
                child.setDescendantUnselected()
            }
        }
    }

    override fun setDescendantSelected() {
        if(isSelected){
            this.setUnselected()
        }
        else if(hasSelectedDescendant){
            this.setDescendantUnselected()
        }
    }
}