package com.example.noteapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.databinding.ActivityPrivacyPolicyBinding

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