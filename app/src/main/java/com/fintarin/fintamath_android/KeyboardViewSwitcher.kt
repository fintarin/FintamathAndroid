package com.fintarin.fintamath_android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.widget.TableRow
import androidx.appcompat.widget.AppCompatButton
import com.fintarin.fintamath_android.databinding.ActivityMainBinding
import kotlinx.coroutines.delay

class KeyboardViewSwitcher {
    companion object{
        fun setKeyboardToDefaultView(binding: ActivityMainBinding, context: Context,mainExpression: MainExpression){
            binding.letters.setBackgroundResource(R.drawable.borders)
            binding.functions.setBackgroundResource(R.drawable.borders)
            binding.functions.setOnClickListener {
                setKeyboardToFunctionView(binding,context,mainExpression)
            }
            binding.letters.setOnClickListener {
                setKeyboardToLettersView(binding,context,mainExpression)
            }
            binding.firstRow.removeAllViews()
            binding.secondRow.removeAllViews()
            binding.thirdRow.removeAllViews()
            binding.fourthRow.removeAllViews()

            binding.firstRow.weightSum=6f
            binding.firstRow.addView(createNumericButton("1",context,binding,mainExpression))
            binding.firstRow.addView(createNumericButton("2",context,binding,mainExpression))
            binding.firstRow.addView(createNumericButton("3",context,binding,mainExpression))
            binding.firstRow.addView(createButton("/",context,binding,mainExpression))
            binding.firstRow.addView(createButton("(",context,binding,mainExpression))
            binding.firstRow.addView(createButton(")",context,binding,mainExpression))

            binding.secondRow.weightSum=6f
            binding.secondRow.addView(createNumericButton("4",context,binding,mainExpression))
            binding.secondRow.addView(createNumericButton("5",context,binding,mainExpression))
            binding.secondRow.addView(createNumericButton("6",context,binding,mainExpression))
            binding.secondRow.addView(createButton("*",context,binding,mainExpression))
            binding.secondRow.addView(createButton("sqrt",context,binding,mainExpression))
            binding.secondRow.addView(createButton("^",context,binding,mainExpression))

            binding.thirdRow.weightSum=6f
            binding.thirdRow.addView(createNumericButton("7",context,binding,mainExpression))
            binding.thirdRow.addView(createNumericButton("8",context,binding,mainExpression))
            binding.thirdRow.addView(createNumericButton("9",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("+",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("x",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("pi",context,binding,mainExpression))

            binding.fourthRow.weightSum=6f
            binding.fourthRow.addView(createLongHoldButton(".",",",context,binding,mainExpression))
            binding.fourthRow.addView(createNumericButton("0",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("=",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("-",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("%",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("e",context,binding,mainExpression))

        }
        fun setKeyboardToFunctionView(binding:ActivityMainBinding,context: Context,mainExpression: MainExpression) {
            binding.letters.setBackgroundResource(R.drawable.borders)
            binding.functions.setBackgroundResource(R.drawable.numbers)
            binding.functions.setOnClickListener {
                KeyboardViewSwitcher.setKeyboardToDefaultView(binding,context,mainExpression)
            }
            binding.letters.setOnClickListener {
                setKeyboardToLettersView(binding,context,mainExpression)
            }
            binding.firstRow.removeAllViews()
            binding.secondRow.removeAllViews()
            binding.thirdRow.removeAllViews()
            binding.fourthRow.removeAllViews()

            binding.firstRow.weightSum=4f
            binding.firstRow.addView(createButton("asin",context,binding,mainExpression))
            binding.firstRow.addView(createButton("sin",context,binding,mainExpression))
            binding.firstRow.addView(createButton("log",context,binding,mainExpression))
            binding.firstRow.addView(createButton("exp",context,binding,mainExpression))

            binding.secondRow.weightSum=4f
            binding.secondRow.addView(createButton("acos",context,binding,mainExpression))
            binding.secondRow.addView(createButton("cos",context,binding,mainExpression))
            binding.secondRow.addView(createButton("ln",context,binding,mainExpression))
            binding.secondRow.addView(createButton("abs",context,binding,mainExpression))

            binding.thirdRow.weightSum=4f
            binding.thirdRow.addView(createButton("atan",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("tan",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("lg",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("!",context,binding,mainExpression))

            binding.fourthRow.weightSum=4f
            binding.fourthRow.addView(createButton("acot",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("cot",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("lb",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("!!",context,binding,mainExpression))
        }


        fun setKeyboardToLettersView(binding:ActivityMainBinding,context: Context,mainExpression: MainExpression) {
            binding.letters.setBackgroundResource(R.drawable.numbers)
            binding.functions.setBackgroundResource(R.drawable.borders)
            binding.functions.setOnClickListener {
                setKeyboardToFunctionView(binding,context,mainExpression)
            }
            binding.letters.setOnClickListener {
                KeyboardViewSwitcher.setKeyboardToDefaultView(binding,context,mainExpression)
            }
            binding.firstRow.removeAllViews()
            binding.secondRow.removeAllViews()
            binding.thirdRow.removeAllViews()
            binding.fourthRow.removeAllViews()

            binding.firstRow.weightSum=7f
            binding.firstRow.addView(createButton("a",context,binding,mainExpression))
            binding.firstRow.addView(createButton("b",context,binding,mainExpression))
            binding.firstRow.addView(createButton("c",context,binding,mainExpression))
            binding.firstRow.addView(createButton("d",context,binding,mainExpression))
            binding.firstRow.addView(createButton("e",context,binding,mainExpression))
            binding.firstRow.addView(createButton("f",context,binding,mainExpression))
            binding.firstRow.addView(createButton("g",context,binding,mainExpression))

            binding.secondRow.weightSum=6f
            binding.secondRow.addView(createButton("h",context,binding,mainExpression))
            binding.secondRow.addView(createButton("i",context,binding,mainExpression))
            binding.secondRow.addView(createButton("j",context,binding,mainExpression))
            binding.secondRow.addView(createButton("k",context,binding,mainExpression))
            binding.secondRow.addView(createButton("l",context,binding,mainExpression))
            binding.secondRow.addView(createButton("m",context,binding,mainExpression))

            binding.thirdRow.weightSum=7f
            binding.thirdRow.addView(createButton("n",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("o",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("p",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("q",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("r",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("s",context,binding,mainExpression))
            binding.thirdRow.addView(createButton("t",context,binding,mainExpression))

            binding.fourthRow.weightSum=6f
            binding.fourthRow.addView(createButton("u",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("v",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("w",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("x",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("y",context,binding,mainExpression))
            binding.fourthRow.addView(createButton("z",context,binding,mainExpression))
        }
        @SuppressLint("RestrictedApi")
        private fun createButton(text: String, context: Context,binding: ActivityMainBinding,mainExpression: MainExpression):AppCompatButton{
            val scale = context.resources.displayMetrics.density
            val button=AppCompatButton(context)
            button.text=text
            button.setAutoSizeTextTypeUniformWithConfiguration(12,100,2,TypedValue.COMPLEX_UNIT_DIP)
            button.setPadding((10*scale+0.5f).toInt(),(10*scale+0.5f).toInt(),(10*scale+0.5f).toInt(),(10*scale+0.5f).toInt())
            button.minHeight=(50*scale+0.5f).toInt()
            button.isAllCaps=false
            button.setTextAppearance(androidx.appcompat.R.style.Widget_AppCompat_Button_Colored)
            button.layoutParams=TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,170,1f)
            button.setTypeface(button.getTypeface(), Typeface.NORMAL);
            button.setBackgroundResource(R.drawable.borders)
            button.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            button.setOnClickListener {
                var x=Symbol(button.text.toString())
                mainExpression.addChild(x)
                binding.toSolve.text=mainExpression.getText()
                x.getLayout().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                binding.scroll.scrollBy(x.getLayout().measuredWidth,0)
            }
            button.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            button.height=button.width
            return button
        }
        private fun createNumericButton(text:String, context: Context,binding: ActivityMainBinding,mainExpression: MainExpression):AppCompatButton{
            val button:AppCompatButton= createButton(text,context,binding,mainExpression)
            button.setBackgroundResource(R.drawable.numbers)
            return button
        }
        private fun createLongHoldButton(text: String,other:String,context: Context,binding: ActivityMainBinding,mainExpression: MainExpression):AppCompatButton{
            val button:AppCompatButton= createButton(text,context,binding,mainExpression)
            button.setOnLongClickListener{
                var x=Symbol(other)
                mainExpression.addChild(x)
                binding.toSolve.text=mainExpression.getText()
                x.getLayout().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                binding.scroll.scrollBy(x.getLayout().measuredWidth,0)
                true
            }
            return button
        }
    }
}