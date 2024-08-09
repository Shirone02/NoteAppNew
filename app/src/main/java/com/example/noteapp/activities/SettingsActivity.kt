package com.example.noteapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener { finish() }

    }
}