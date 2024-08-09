package com.example.noteapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    private val binding: ActivityHelpBinding by lazy {
        ActivityHelpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener { finish() }

    }
}