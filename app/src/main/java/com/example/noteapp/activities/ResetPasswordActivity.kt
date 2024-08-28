package com.example.noteapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.ActivityNavigatorExtras
import com.example.noteapp.R
import com.example.noteapp.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private val binding: ActivityResetPasswordBinding by lazy {
        ActivityResetPasswordBinding.inflate(layoutInflater)
    }

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.resetPasswordBtn.setOnClickListener {
            val forgotEmail = binding.forgotEmail.text.toString()

            mAuth.sendPasswordResetEmail(forgotEmail).addOnSuccessListener {
                Toast.makeText(this, "Đặt lại mật khẩu thành công. Vui lòng kiểm tra email của bạn", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()

            }.addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }

    }
}