package com.example.noteapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.noteapp.R
import com.example.noteapp.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class SignInActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private val binding: ActivitySignInBinding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }

    var valid = true
    private lateinit var mAuth: FirebaseAuth
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        setVariable()

        binding.tvForgotPassword.setOnClickListener {
            startActivity( Intent(this, ResetPasswordActivity::class.java))
        }
//
//        if (currentUser != null) {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }

//        val signInButton = findViewById<Button>(R.id.signInButton)
//        signInButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.blue)
//        signInButton.setOnClickListener {
//            signIn()
//        }
    }

    private fun setVariable() {
        binding.signIn.setOnClickListener{
            if(valid){
                val email = binding.userEdt.getText().toString()
                val password = binding.passEdt.getText().toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this@SignInActivity) { task ->
                        if (task.isSuccessful) {
                            val verification = mAuth.currentUser?.isEmailVerified
                            if(verification == true){
                                startActivity(Intent(applicationContext, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Vui lòng xác nhận email của bạn", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@SignInActivity, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@SignInActivity, "Vui lòng điền email và mật khẩu", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvGotoRegister.setOnClickListener { v: View? ->
            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            finish()
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)
//                firebaseAuthWithGoogle(account.idToken!!)
//            } catch (e: ApiException) {
//
//            }
//        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

