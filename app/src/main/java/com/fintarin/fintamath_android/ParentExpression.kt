package com.fintarin.fintamath_android

interface ParentExpression:Expression {
    fun hasSelectedDescendant():Boolean
    fun addPointerChild()
    fun addPointerChild(position:Int)
    fun addChild(childExpression: ChildExpression,child:ChildExpression)
    fun deletePointerChild()
    fun deleteChild()
    fun addChild(childExpression: ChildExpression)
    fun setDescendantUnselected()
    fun setDescendantSelected()
}