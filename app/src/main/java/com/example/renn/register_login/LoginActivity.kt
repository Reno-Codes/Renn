package com.example.renn.register_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.renn.MainActivity
import com.example.renn.R
import com.example.renn.helpers.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var tvRedirectSignUp: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var btnLogin: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // View Bindings
        etEmail = findViewById(R.id.etEmailAddress)
        etPass = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRedirectSignUp = findViewById(R.id.tvRedirectSignUp)


        btnLogin.setOnClickListener {
            login()
        }

        tvRedirectSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login() {
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        // calling signInWithEmailAndPassword(email, pass)
        // function using Firebase auth object
        // On successful response Display a Toast
        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Email / password can't be blank", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else
                Toast.makeText(this, "Login failed. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

}
