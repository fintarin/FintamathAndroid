package com.example.fintamath_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.fintamath_android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        button=findViewById(R.id.find_solution)
        button.setOnClickListener { view: View ->
            binding.solution.text = findSolution(binding.toSolve.text.toString())
        }
    }

    external fun findSolution(string: String): String

    companion object {
        // Used to load the 'fintamath_android' library on application startup.
        init {
            System.loadLibrary("fintamath_android")
        }
    }
}