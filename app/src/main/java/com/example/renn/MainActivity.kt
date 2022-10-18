package com.example.renn

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var captionTv: TextView
    private lateinit var jobAlertBtn: Button
    private lateinit var jobEt: EditText
    private lateinit var sendJobBtn: Button
    private lateinit var btnSignOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        captionTv = findViewById(R.id.captionTv)
        jobAlertBtn = findViewById(R.id.jobAlertBtn)
        jobEt = findViewById(R.id.jobEt)
        sendJobBtn = findViewById(R.id.sendJobBtn)
        btnSignOut = findViewById(R.id.signOutBtn)

        checkLoggedIn()

        btnSignOut.setOnClickListener {
            Toast.makeText(this, "Signed out.", Toast.LENGTH_SHORT).show()
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // using finish() to end the activity
            finish()
        }

        jobAlertBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        sendJobBtn.setOnClickListener {
            if (jobEt.text.isEmpty()){
                Toast.makeText(this, "Job can't be empty!", Toast.LENGTH_SHORT).show()
                Log.d("jobEt", "Job Edit Text: Job posting text is empty")
            }
            else{
                database = FirebaseDatabase.getInstance().getReference("Users")
                val userid = FirebaseAuth.getInstance().currentUser!!.uid
                val job = Job(jobEt.text.toString())
                database.child(userid).child("Jobs").setValue(job).addOnSuccessListener {
                    Log.d("JobPostToDB", "JobPostDB: Job posted")
                    Toast.makeText(this, "Job posted!", Toast.LENGTH_SHORT).show()
                    jobEt.text.clear()
                }.addOnFailureListener {
                    Log.d("JobPostToDB", "JobPostDB: Failed posting job")
                }

            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun checkLoggedIn(){
        if(auth.currentUser == null){
            Log.d("checkLoggedInTAG", "checkLoggedIn: User not logged in")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // using finish() to end the activity
            finish()
        }
        else{
            captionTv.text = "Signed in as\n${auth.currentUser!!.email}"
        }
    }
}