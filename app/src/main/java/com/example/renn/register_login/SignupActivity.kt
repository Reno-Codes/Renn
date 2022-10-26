package com.example.renn.register_login

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.renn.Categories
import com.example.renn.MainActivity
import com.example.renn.R
import com.example.renn.User
import com.google.android.gms.common.SignInButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLoc: LatLng

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
        googleSignInBtn = findViewById(R.id.google_singIn_Btn)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Default location
        userLoc = LatLng(0.0, 0.0)

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

            if (!checkPermission()) {
                permissionDialog()
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
                val userid = FirebaseAuth.getInstance().currentUser!!.uid
                val user = User(
                    email = email,
                    userid = userid,
                    workEnabled = false,
                    locationLatitude = userLoc.latitude,
                    locationLongitude = userLoc.longitude
                )
                val userCategories = Categories(homeCat = false, taxiCat = false)

                // Add to User with details to Realtime Database
                database.child(userid).setValue(user).addOnSuccessListener {
                    // Set Users Location to db
                    getLocation()
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

    // Check Permission
    @SuppressLint("MissingPermission")
    private fun checkPermission(): Boolean{
        return if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            false
        }
        else{
            true
        }
    }

    // Get Permission
    @SuppressLint("MissingPermission")
    private fun getPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            101
        )
    }

    // Permission alert dialog
    private fun permissionDialog(){
        val dialogBuilder = AlertDialog.Builder(this)

        // set message of alert dialog
        dialogBuilder.setMessage("For app to work properly, please allow location permission!")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Ok") { _, _ ->
                getPermission()
            }

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("Location permission required!")
        // show alert dialog
        alert.show()
    }

    // Get user location and save it to User's table in db
    @SuppressLint("MissingPermission")
    private fun getLocation(){
        val userid = FirebaseAuth.getInstance().currentUser!!.uid
        @Suppress("DEPRECATION")
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false }).addOnSuccessListener { location: Location? ->
            if (location == null)
                Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
            else {
                userLoc = LatLng(location.latitude, location.longitude)
                database.child(userid).child("locationLatitude").setValue(userLoc.latitude).addOnSuccessListener {
                    Log.d("SettingUserLocation", "SettingUserLocation: Location Latitude saved to database!")
                    database.child(userid).child("locationLongitude").setValue(userLoc.longitude)
                        .addOnSuccessListener {
                            Log.d("SettingUserLocation", "SettingUserLocation: Location Longitude saved to database!")
                        }.addOnFailureListener {
                            Log.d("SettingUserLocation", "SettingUserLocation: Location Longitude NOT SAVED!")
                        }.addOnFailureListener {
                            Log.d("SettingUserLocation", "SettingUserLocation: Location Latitude NOT SAVED!")
                        }
                }
            }
        }
    }
}
