package com.example.renn

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var captionTv: TextView
    private lateinit var settingsBtn: ImageButton
    private lateinit var jobEt: EditText
    private lateinit var sendJobBtn: Button
    private lateinit var btnSignOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        captionTv = findViewById(R.id.captionTv)
        settingsBtn = findViewById(R.id.settingsBtn)
        jobEt = findViewById(R.id.jobEt)
        sendJobBtn = findViewById(R.id.sendJobBtn)
        btnSignOut = findViewById(R.id.signOutBtn)

        checkLoggedIn()

        btnSignOut.setOnClickListener {
            Toast.makeText(this, "Signed out.", Toast.LENGTH_SHORT).show()
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_down,R.anim.slide_up)
            // using finish() to end the activity
            finish()
        }

        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
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
                database.child("All_Categories").child("HomeCategory").child("Posted_jobs").child(userid).setValue(userid)
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
            Toast.makeText(this,"You're logged out..", Toast.LENGTH_SHORT).show()
            finish()
        }
        else{
            captionTv.text = "${auth.currentUser!!.email}"
        }
    }
}