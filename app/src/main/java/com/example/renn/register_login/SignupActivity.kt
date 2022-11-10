package com.example.renn.register_login

import android.annotation.SuppressLint
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
import com.example.renn.utils.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng


@Suppress("RedundantIf")
class SignupActivity : AppCompatActivity() {

    // View bindings
    private lateinit var etEmail: EditText
    private lateinit var etConfPass: EditText
    private lateinit var etPass: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvRedirectLogin: TextView
    private lateinit var tvSignupWelcome: TextView
    private lateinit var googleSignInBtn: SignInButton


    private lateinit var fusedLocationClient: FusedLocationProviderClient


    // Email Pass Validators
    private val validator = EmailPassValidatorUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // View Bindings
        etEmail = findViewById(R.id.etSEmailAddress)
        etConfPass = findViewById(R.id.etSConfPassword)
        etPass = findViewById(R.id.etSPassword)
        btnSignUp = findViewById(R.id.btnSSigned)
        tvRedirectLogin = findViewById(R.id.tvRedirectLogin)
        tvSignupWelcome = findViewById(R.id.tvSignupWelcome)
        googleSignInBtn = findViewById(R.id.google_singIn_Btn)



        // Sign Up button click listener
        btnSignUp.setOnClickListener {

            tvSignupWelcome.text = getString(R.string.sign_up)
            btnSignUp.text = getString(R.string.sign_up)

            // Show email and password editTexts
            etEmail.visibility = View.VISIBLE
            etPass.visibility = View.VISIBLE
            etConfPass.visibility = View.VISIBLE

            // First check if Location permission is allowed
            // then show alert dialog and ask for permission
            if (!checkPermission(this)) {
                showDialogAndGetPermission(this, this@SignupActivity)
            }
            else{
                signUpUser()
            }
        }

        // switching from signUp Activity to Login Activity
        tvRedirectLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    @SuppressLint("MissingPermission")
    private fun signUpUser() {
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        val confirmPassword = etConfPass.text.toString()

        val isEmailValid = validator.isValidEmail(email)
        val isPasswordValid = validator.isValidPassword(pass)


        // Check if fields are empty
        if (email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Empty email or password", Toast.LENGTH_SHORT).show()
            return
        }

        // Is email in valid format?
        if (!isEmailValid){
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        // Is password strong enough?
        if (!isPasswordValid){
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
            validator.passwordAlert(this)
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
                // Get UserId
                val userid = auth.currentUser!!.uid
                Toast.makeText(this, "Successfully signed Up", Toast.LENGTH_SHORT).show()
                val user = User(
                    email = email,
                    userid = userid,
                    userCircleRadius = 1.0, // default kilometer
                    workEnabled = false
                )
                // User default categories
                val userCategories = Categories(
                    beautyCat = false,
                    homeCat = false,
                    taxiCat = false
                )

                // Add to User to Realtime Database
                val usersRef = database.child("Users")

                usersRef.child(userid).setValue(user).addOnSuccessListener {
                    // Set Users Current Location to db
                    updateCurrentUserLocation(this, fusedLocationClient)
                    Log.d("PushUserToDB", "PushUserToDB: User saved to database!")
                    // Add user's default settings for categories to database
                    usersRef.child(userid).child("Categories").setValue(userCategories)
                }.addOnFailureListener {
                    Log.d("PushUserToDB", "PushUserToDB: Failed saving to database.")
                }

                // Sign in
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { login ->
                    if (login.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
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
