package com.fintarin.fintamath_android

import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher

import com.fintarin.fintamath_android.databinding.ActivityMainBinding
import java.lang.Exception
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity() {
    private val TAG:String="Main activity calls calculator"
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentJob: Job
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
        binding.toSolve.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Dispatchers.Main.cancelChildren()
                currentJob=lifecycleScope.launch(Dispatchers.Main) {
                    val result:String= withContext(Dispatchers.Default){
                            calculate(binding.toSolve.text.toString())}
                    if(currentJob.isActive) {
                        binding.solution.text = result
                    }
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
}