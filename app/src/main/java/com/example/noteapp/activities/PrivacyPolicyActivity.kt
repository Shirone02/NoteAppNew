package com.example.noteapp.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.noteapp.R
import com.example.noteapp.databinding.ActivityPrivacyPolicyBinding
import com.example.noteapp.databinding.ActivitySettingsBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    private val binding: ActivityPrivacyPolicyBinding by lazy {
        ActivityPrivacyPolicyBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener { finish() }

    }
}