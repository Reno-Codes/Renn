package com.example.renn.register_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.renn.Categories
import com.example.renn.MainActivity
import com.example.renn.R
import com.example.renn.User
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etConfPass: EditText
    private lateinit var etPass: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvRedirectLogin: TextView
    private lateinit var tvSignupWelcome: TextView
    private lateinit var google_signIn_Btn: SignInButton

    // create Firebase authentication object
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // View Bindings
        etEmail = findViewById(R.id.etSEmailAddress)
        etConfPass = findViewById(R.id.etSConfPassword)
        etPass = findViewById(R.id.etSPassword)
        btnSignUp = findViewById(R.id.btnSSigned)
        tvRedirectLogin = findViewById(R.id.tvRedirectLogin)
        tvSignupWelcome = findViewById(R.id.tvSignupWelcome)
        google_signIn_Btn = findViewById(R.id.google_singIn_Btn)

        // Initialising auth object
        auth = Firebase.auth

        // Sign Up button click listener
        btnSignUp.setOnClickListener {
            tvSignupWelcome.text = getString(R.string.sign_up)
            btnSignUp.text = getString(R.string.sign_up)
            // Show email and password editTexts
            etEmail.visibility = View.VISIBLE
            etPass.visibility = View.VISIBLE
            etConfPass.visibility = View.VISIBLE

            signUpUser()
        }

        // switching from signUp Activity to Login Activity
        tvRedirectLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
        }
    }


    private fun signUpUser() {
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        val confirmPassword = etConfPass.text.toString()

        @Suppress("RegExpRedundantEscape", "RegExpDuplicateCharacterInClass")
        val emailAddressPattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        // Is email correct
        fun isValidString(str: String): Boolean{
            return emailAddressPattern.matcher(str).matches()
        }

        // check pass
        if (email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidString(email)){
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmPassword) {
            Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT)
                .show()
            return
        }
        // If all credential are correct
        // We call createUserWithEmailAndPassword
        // using auth object and pass the
        // email and pass in it.
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) { signup ->
            if (signup.isSuccessful) {
                Toast.makeText(this, "Successfully signed Up", Toast.LENGTH_SHORT).show()
                database = FirebaseDatabase.getInstance().getReference("Users")
                val userid= FirebaseAuth.getInstance().currentUser!!.uid

                val user = User(email = email, userid = userid, workEnabled = false)
                val userCategories = Categories(homeCat = false, taxiCat = false)

                // Add to User with details to Realtime Database
                database.child(userid).setValue(user).addOnSuccessListener {
                    Log.d("PushEmailToDB", "signUpUser: Saved to database!")
                    database.child(userid).child("Categories").setValue(userCategories)
                }.addOnFailureListener {
                    Log.d("PushEmailToDB", "signUpUser: Failed saving to database.")
                }

                // Sign in
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { login ->
                    if (login.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        // using finish() to end the activity
                        finish()

                    } else {
                        Toast.makeText(this, "Can't login. Something went wrong!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else {
                Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
