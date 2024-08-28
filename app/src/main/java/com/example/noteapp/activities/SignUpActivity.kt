package com.example.noteapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.noteapp.R
import com.example.noteapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    private var valid = true
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setVariable()
    }

    private fun setVariable() {
        binding.signUp.setOnClickListener {
            val email = binding.userEdt.text.toString()
            val password = binding.passEdt.text.toString()

            if(valid){
                if (password.length < 6) {
                    Toast.makeText(this@SignUpActivity, "Độ dài mật khẩu phải có ít nhất 6 kí tự", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this@SignUpActivity) { task->
                    if (task.isSuccessful) {
                        mAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                            Toast.makeText(this, "Vui lòng xác nhận email của bạn", Toast.LENGTH_SHORT).show()

                            val user = mAuth.currentUser
                            Toast.makeText(this, "Tài khoản được tạo thành công", Toast.LENGTH_SHORT).show()
                            val myRef = database.getReference("Users").child(user!!.uid)
                            val userInfo: MutableMap<String, Any> = HashMap()
                            userInfo["Email"] = email
                            userInfo["Password"] = password
                            myRef.setValue(userInfo)
                            startActivity(Intent(applicationContext, SignInActivity::class.java))
                            finish()

                        }?.addOnFailureListener {
                            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this@SignUpActivity, "Tạo tài khoản lỗi", Toast.LENGTH_SHORT).show()
                    }
                }

                binding.tvGotoLogin.setOnClickListener { v: View? ->
                    startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                    finish()
                }
            }
        }
    }
}