package com.example.noteapp.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.noteapp.R
import com.example.noteapp.databinding.ActivityBackupBinding

class BackupActivity : AppCompatActivity() {

    private val binding: ActivityBackupBinding by lazy {
        ActivityBackupBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener { finish() }

    }
}