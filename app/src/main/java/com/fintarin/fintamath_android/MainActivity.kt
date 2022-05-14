package com.fintarin.fintamath_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View


import com.fintarin.fintamath_android.databinding.ActivityMainBinding
import java.lang.Exception
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope

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


        binding.mainForm.setOnClickListener{changeKeyboardVisibility()}

        binding.toSolve.setOnClickListener{changeKeyboardVisibility()}

        binding.solution.setOnClickListener{changeKeyboardVisibility()}

        binding.toSolve.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                toSolve=expression.getText()
                Log.d("sd","Start calculation {")
                try {
                    if (toSolve !== "") {
                        Dispatchers.Main.cancelChildren()
                        currentJob = lifecycleScope.launch(Dispatchers.Main) {
                            val result: String = withContext(Dispatchers.Default) {
                                calculate(toSolve)
                            }
                            if (currentJob.isActive) {
                                binding.solution.text = result
                            }
                        }
                    }
                }
                catch (e:Exception){
                    Log.d("DJ-Tape","Catch some shit")
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
            binding.functionalRow.visibility= View.GONE
            binding.keyboard.visibility=View.GONE
            isKeyboardVisible=false
        }
        else{
            binding.functionalRow.visibility= View.VISIBLE
            binding.keyboard.visibility=View.VISIBLE
            isKeyboardVisible=true
        }
    }
}