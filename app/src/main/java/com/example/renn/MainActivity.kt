package com.example.renn

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.renn.maps.MapsActivity
import com.example.renn.register_login.LoginActivity
import com.example.renn.settings.SettingsActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var captionTv: TextView
    private lateinit var jobEt: EditText

    private lateinit var settingsBtn: ImageButton
    private lateinit var sendJobBtn: Button
    private lateinit var btnSignOut: Button
    private lateinit var btnMap: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        captionTv = findViewById(R.id.captionTv)
        settingsBtn = findViewById(R.id.settingsBtn)
        jobEt = findViewById(R.id.jobEt)
        sendJobBtn = findViewById(R.id.sendJobBtn)
        btnSignOut = findViewById(R.id.signOutBtn)
        btnMap = findViewById(R.id.mapBtn)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check if user is signed in
        checkLoggedIn()

        // Sign out button
        btnSignOut.setOnClickListener {
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
            if (jobEt.text.isEmpty()){
                Toast.makeText(this, "Job can't be empty!", Toast.LENGTH_SHORT).show()
                Log.d("jobEt", "Job Edit Text: Job posting text is empty")
            }
            else{
                database = FirebaseDatabase.getInstance().getReference("Users")
                val userid = FirebaseAuth.getInstance().currentUser!!.uid
                val job = Job(jobEt.text.toString())
                // Post job to general category All_Categories
                database.child("All_Categories").child("HomeCategory").child("Posted_jobs").child(userid).setValue(userid)
                    .addOnSuccessListener {
                    // Post jon to user's Jobs table
                    database.child(userid).child("Jobs").setValue(job)
                        .addOnSuccessListener {
                        Log.d("JobPostToDB", "JobPostDB: Job posted")
                        Toast.makeText(this, "Job posted!", Toast.LENGTH_SHORT).show()
                        jobEt.text.clear()
                    }
                }.addOnFailureListener {
                    Log.d("JobPostToDB", "JobPostDB: Failed posting job")
                }
            }
        }


        fun getSetLocation() {
            Toast.makeText(this@MainActivity, "Updating location...", Toast.LENGTH_SHORT).show()
            database = FirebaseDatabase.getInstance().getReference("Users")
            val userid = FirebaseAuth.getInstance().currentUser!!.uid
            @Suppress("DEPRECATION")
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                }).addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {

                    // Set user's location latitude
                    database.child(userid).child("locationLatitude").setValue(location.latitude)
                        .addOnSuccessListener {
                            Log.d(
                                "SettingUserLocation",
                                "SettingUserLocation: Location Latitude saved to database!"
                            )
                            // Set user's location longitude
                            database.child(userid).child("locationLongitude")
                                .setValue(location.longitude)
                                .addOnSuccessListener {
                                    Toast.makeText(this@MainActivity, "Location updated!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MapsActivity::class.java)
                                    startActivity(intent)
                                    Log.d(
                                        "SettingUserLocation",
                                        "SettingUserLocation: Location Longitude saved to database!"
                                    )
                                }.addOnFailureListener {
                                    Log.d(
                                        "SettingUserLocation",
                                        "SettingUserLocation: Location Longitude NOT SAVED!"
                                    )
                                }.addOnFailureListener {
                                    Log.d(
                                        "SettingUserLocation",
                                        "SettingUserLocation: Location Latitude NOT SAVED!"
                                    )
                                }
                        }
                }
            }
        }

        // Btn Update location
        btnMap.setOnClickListener {
            if (!checkPermission()) {
                permissionDialog()
            } else {
                getSetLocation()
            }
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
            captionTv.text = "${auth.currentUser!!.email}"
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