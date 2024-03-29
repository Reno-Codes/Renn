package com.example.renn

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.renn.categories.CategoryActivity
import com.example.renn.utils.*
import com.example.renn.profile.ProfileActivity
import com.example.renn.register_login.LoginActivity
import com.example.renn.settings.SettingsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity() {

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


    /* onCreate */
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
        if(isUserSignedIn()){
            val currentUserEmail = auth.currentUser?.email
            tvEmail.text = "$currentUserEmail"
        }
        else{
            Log.d("checkLoggedInTAG", "checkLoggedIn: User not logged in")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(this,"Please sign in..", Toast.LENGTH_SHORT).show()
            finish()
        }

        val currentUserId = auth.currentUser?.uid
        val usersRef = database.child("Users")


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
                usersRef.child("All_Categories")
                    .child("HomeCategory")
                    .child("Posted_jobs")
                    .child(currentUserId!!).setValue(currentUserId)
                    .addOnSuccessListener {
                    // Post jon to user's Jobs table
                        usersRef.child(currentUserId).child("Jobs").setValue(job)
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
}