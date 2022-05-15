package com.fintarin.fintamath_android

import android.icu.number.IntegerWidth
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View


import com.fintarin.fintamath_android.databinding.ActivityMainBinding
import java.lang.Exception
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import java.math.BigDecimal
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var expression: MainExpression
    private lateinit var currentJob: Job
    private var toSolve:String=""
    private var isKeyboardVisible:Boolean=true
    private fun calculate(string: String):String{
        return try {
            findSolution(string)
        } catch (e:Exception){
            "Unable to calculate"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        expression= MainExpression(binding.newview,binding)
        KeyboardViewSwitcher.setKeyboardToDefaultView(binding, this,expression)

        binding.delete.setOnClickListener {
            expression.deleteChild()
            binding.toSolve.text=expression.getText()
        }
        binding.fullSolution.setOnClickListener{
            if(binding.fullSolutionText.visibility==View.VISIBLE) {
                binding.fullSolution.text="Show full solution"
                binding.fullSolutionText.visibility = View.INVISIBLE
            }
            else{
                binding.fullSolution.text="Hide full solution"
                binding.fullSolutionText.visibility = View.VISIBLE
            }
        }



        binding.mainForm.setOnClickListener{changeKeyboardVisibility()}

        binding.toSolve.setOnClickListener{changeKeyboardVisibility()}

        binding.solution.setOnClickListener{changeKeyboardVisibility()}

        binding.fullSolutionText.movementMethod=ScrollingMovementMethod.getInstance()
        binding.solution.movementMethod=ScrollingMovementMethod.getInstance()
        binding.solution.setHorizontallyScrolling(true)


        binding.toSolve.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                toSolve=expression.getText()
                try {
                    if (toSolve !== "") {
                        binding.progressBar.visibility=View.VISIBLE
                        binding.solution.visibility=View.INVISIBLE
                        binding.fullSolution.text="Show full solution"
                        binding.fullSolutionText.visibility = View.INVISIBLE
                        Dispatchers.Main.cancelChildren()
                        currentJob = lifecycleScope.launch(Dispatchers.Main) {
                            delay(300L)

                            var result: String = withContext(Dispatchers.Default) {
                                calculate(toSolve)
                            }
                            if (currentJob.isActive) {
                                binding.progressBar.visibility = View.INVISIBLE
                                binding.solution.visibility = View.VISIBLE
                                binding.fullSolutionText.text = result
                                if (result.length > 20) {
                                    try {
                                        result.toBigDecimal()
                                        if (result.contains("-")) {
                                            binding.solution.text = "-"
                                            result=result.replace("-", "")
                                        }
                                        var dotPosition =result.length
                                            if (result.contains(".")) {
                                            dotPosition = result.indexOf(".")
                                            result=result.replace(".", "")
                                        }
                                        binding.solution.text=binding.solution.text.toString()+result[0]+"."+result.substring(1, 19)+"*10^${dotPosition}"
                                        binding.fullSolution.visibility = View.VISIBLE
                                        binding.fullSolutionText.visibility = View.INVISIBLE

                                    } catch (e: Exception) {
                                            binding.fullSolution.visibility = View.VISIBLE
                                            binding.fullSolutionText.visibility = View.INVISIBLE
                                            binding.solution.text = result.substring(0, 19) + "..."
                                    }
                                    Log.d("Calculate", "${toSolve} , ${result}")
                                }
                                else{
                                    binding.solution.text=result
                                    binding.fullSolution.visibility = View.INVISIBLE
                                    binding.fullSolutionText.visibility = View.INVISIBLE
                                }
                            }
                        }
                    }
                }
                catch (e:Exception){
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    external fun findSolution(string: String): String

    companion object {
        init {
            System.loadLibrary("fintamath-android")
        }
    }

    private fun changeKeyboardVisibility(){
        if(isKeyboardVisible){
            binding.functionalRow.visibility= View.INVISIBLE
            binding.keyboard.visibility=View.INVISIBLE
            isKeyboardVisible=false
        }
        else{
            binding.functionalRow.visibility= View.VISIBLE
            binding.keyboard.visibility=View.VISIBLE
            isKeyboardVisible=true
        }
    }
}