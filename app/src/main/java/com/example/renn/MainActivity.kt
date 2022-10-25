package com.example.renn

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.renn.categories.CategoryActivity
import com.example.renn.profile.ProfileActivity
import com.example.renn.register_login.LoginActivity
import com.example.renn.settings.SettingsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    // Auth and Database
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseUsersRef: DatabaseReference

    // TextViews and EditTexts
    private lateinit var tvEmail: TextView
    private lateinit var etJob: EditText

    // Buttons
    private lateinit var settingsBtn: ImageView
    private lateinit var sendJobBtn: Button
    private lateinit var signOutBtn: Button
    private lateinit var profileBtn: Button
    private lateinit var categoriesBtn: Button

    // Fused Location Provider Client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind Auth and Database
        auth = FirebaseAuth.getInstance()
        val userid = auth.currentUser?.uid
        databaseUsersRef = FirebaseDatabase.getInstance().getReference("Users")

        // Bind TextViews and EditTexts
        tvEmail = findViewById(R.id.tvEmail)
        etJob = findViewById(R.id.jobEt)

        // Bind Buttons
        settingsBtn = findViewById(R.id.settingsBtn)
        sendJobBtn = findViewById(R.id.sendJobBtn)
        signOutBtn = findViewById(R.id.signOutBtn)
        profileBtn = findViewById(R.id.profileBtn)
        categoriesBtn = findViewById(R.id.categoriesBtn)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check if user is signed in
        checkLoggedIn()


        // Category button
        categoriesBtn.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
        }
        // Sign out button
        signOutBtn.setOnClickListener {
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            // Sign out
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // finish() to end current activity
            finish()
        }

        // Settings button
        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Post job button
        sendJobBtn.setOnClickListener {
            if (etJob.text.isEmpty()){
                Toast.makeText(this, "Job can't be empty!", Toast.LENGTH_SHORT).show()
                Log.d("jobEt", "Job Edit Text: Job posting text is empty")
            }
            else{
                val job = Job(etJob.text.toString())
                // Post job to general category All_Categories
                databaseUsersRef.child("All_Categories").child("HomeCategory").child("Posted_jobs").child(userid!!).setValue(userid)
                    .addOnSuccessListener {
                    // Post jon to user's Jobs table
                    databaseUsersRef.child(userid).child("Jobs").setValue(job)
                        .addOnSuccessListener {
                        Log.d("JobPostToDB", "JobPostDB: Job posted")
                        Toast.makeText(this, "Job posted!", Toast.LENGTH_SHORT).show()
                        etJob.text.clear()
                    }
                }.addOnFailureListener {
                    Log.d("JobPostToDB", "JobPostDB: Failed posting job")
                }
            }
        }




        // Btn Update location
        profileBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

    }

    // Check if user is signed in
    @SuppressLint("SetTextI18n")
    private fun checkLoggedIn(){
        if(auth.currentUser == null){
            Log.d("checkLoggedInTAG", "checkLoggedIn: User not logged in")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(this,"Please sign in..", Toast.LENGTH_SHORT).show()
            finish()
        }
        else{
            tvEmail.text = "${auth.currentUser!!.email}"
        }
    }

    // Check Permission
    @SuppressLint("MissingPermission")
    @Suppress("RedundantIf")
    private fun checkPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else {
            true
        }
    }

    // Get Permission
    @SuppressLint("MissingPermission")
    private fun getPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            101
        )
    }

    // Show Permission alert dialog
    private fun permissionDialog() {
        val dialogBuilder = AlertDialog.Builder(this)

        // set message of alert dialog
        dialogBuilder.setMessage("To update your location, please allow location permission!")
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

}