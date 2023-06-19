package com.example.sonda

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sonda.databinding.ActivityMainBinding
import com.example.sonda.viewModel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}