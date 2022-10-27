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
import com.example.renn.helpers.EmailPassValidatorRepository
import com.example.renn.helpers.FirebaseRepository
import com.example.renn.helpers.MapsRepository
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

    // Google Maps etc
    private val mapsRepository = MapsRepository()

    private lateinit var userLoc: LatLng

    private lateinit var showDialogAndGetPermission : Unit

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Firebase authentication/database
    private val firebase = FirebaseRepository()
    private val auth = firebase.getInstance()

    private val usersRef = firebase.dbRef("Users")

    // Email Pass Validators
    private val validator = EmailPassValidatorRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        // Default location
        userLoc = LatLng(0.0, 0.0)

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
            if (!mapsRepository.checkPermission(this)) {
                showDialogAndGetPermission = mapsRepository.showDialogAndGetPermission(this, this@SignupActivity)
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
                val userid = firebase.currentUserUid()
                Toast.makeText(this, "Successfully signed Up", Toast.LENGTH_SHORT).show()
                val user = User(
                    email = email,
                    userid = userid,
                    workEnabled = false,
                    userLocation = LatLng(userLoc.latitude, userLoc.longitude)
                )
                // User default categories
                val userCategories = Categories(
                    beautyCat = false,
                    homeCat = false,
                    taxiCat = false
                )

                // Add to User to Realtime Database
                usersRef.child(userid!!).setValue(user).addOnSuccessListener {
                    // Set Users Current Location to db
                    mapsRepository.getSetUserCurrentLocation(this, userid, usersRef, fusedLocationClient)
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
