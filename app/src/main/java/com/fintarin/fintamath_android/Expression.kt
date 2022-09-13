package com.fintarin.fintamath_android

import android.view.ViewGroup

interface Expression {
    fun getText():String
    fun isSelected():Boolean
    fun setSelected()
    fun getLayout():ViewGroup
    fun setUnselected()
}