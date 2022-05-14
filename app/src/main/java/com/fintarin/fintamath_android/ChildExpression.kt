package com.fintarin.fintamath_android

interface ChildExpression:Expression {
    fun selfDestruct()
    fun setParent(parent:ParentExpression)
}