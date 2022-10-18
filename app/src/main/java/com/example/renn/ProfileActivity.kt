package com.example.renn

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {
    //private lateinit var binding: ActivityProfileBinding
    //private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var homeSwitch: SwitchMaterial
    private lateinit var taxiSwitch: SwitchMaterial
    private lateinit var workSwitch: SwitchMaterial

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(binding.root)
        setContentView(R.layout.activity_profile)

        homeSwitch = findViewById(R.id.switchHome)
        taxiSwitch = findViewById(R.id.switchTaxi)
        workSwitch = findViewById(R.id.switchWork)

        database = FirebaseDatabase.getInstance().getReference("Users")
        val userid= FirebaseAuth.getInstance().currentUser!!.uid
        //val userEmail= FirebaseAuth.getInstance().currentUser!!.email
        checkCategories(userid)

        // Enable/Disable work switch
        workSwitch.setOnCheckedChangeListener { _, _ ->
            if (workSwitch.isChecked) {
                // Enable Work for user in Database
                database.child(userid).child("workEnabled").setValue(true).addOnSuccessListener {
                    Log.d("workEnabled", "workEnabled: workEnabled = true")
                }.addOnFailureListener {
                    Log.d("workEnabled", "workEnabled: Can't enable work")
                }

                workSwitch.text = "Disable Work"
                workSwitch.setTextColor(Color.parseColor("#0aad3f"))
                homeSwitch.visibility = View.VISIBLE
                taxiSwitch.visibility = View.VISIBLE
                checkCategories(userid)
            }
            else{
                // Disable Work for user in Database
                database.child(userid).child("workEnabled").setValue(false).addOnSuccessListener {
                    Log.d("workEnabled", "workEnabled: workEnabled = false")
                }.addOnFailureListener {
                    Log.d("workEnabled", "workEnabled: Can't disable work")
                }

                workSwitch.text = "Enable Work"
                workSwitch.setTextColor(Color.parseColor("#FBFBFF"))
                homeSwitch.visibility = View.INVISIBLE
                taxiSwitch.visibility = View.INVISIBLE
                database.child(userid).child("Categories").child("homeCat").setValue("OFF")
                database.child(userid).child("Categories").child("taxiCat").setValue("OFF")
                checkCategories(userid)
            }
        }

        // Home Category switch
        homeSwitch.setOnCheckedChangeListener { _, _ ->
            if (homeSwitch.isChecked) {
                database.child(userid).child("Categories").child("homeCat").setValue("ON").addOnSuccessListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat = ON!")
                    database.child("HomeCategory").child(userid).setValue(userid)
                }.addOnFailureListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat can't be updated")
                }
            }
            else{
                database.child(userid).child("Categories").child("homeCat").setValue("OFF").addOnSuccessListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat = OFF!")
                    database.child("HomeCategory").child(userid).setValue(null)
                }.addOnFailureListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat can't be updated")
                }
            }
        }


        taxiSwitch.setOnCheckedChangeListener { _, _ ->
            if (taxiSwitch.isChecked) {
                database.child(userid).child("Categories").child("taxiCat").setValue("ON").addOnSuccessListener {
                    Log.d("UpdatetaxiCat", "updatetaxiCat: taxiCat = ON!")
                    database.child("TaxiCategory").child(userid).setValue(userid)
                }.addOnFailureListener {
                    Log.d("UpdatetaxiCat", "updatetaxiCat: taxiCat can't be updated")
                }
            }
            else{
                database.child(userid).child("Categories").child("taxiCat").setValue("OFF").addOnSuccessListener {
                    Log.d("UpdatetaxiCat", "updatetaxiCat: taxiCat = OFF!")
                    database.child("TaxiCategory").child(userid).setValue(null)
                }.addOnFailureListener {
                    Log.d("UpdatetaxiCat", "updatetaxiCat: taxiCat can't be updated")
                }
            }
        }

    }

    private fun checkCategories(userid: String){
        val uidRef = database.child(userid)
        uidRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val workStatus = snapshot.child("workEnabled").getValue(Boolean::class.java)
                val homeCat = snapshot.child("Categories").child("homeCat").getValue(String::class.java)
                val taxiCat = snapshot.child("Categories").child("taxiCat").getValue(String::class.java)

                // Get work status
                @Suppress("RedundantIf")
                if (workStatus == false){
                    workSwitch.isChecked = false
                }
                else{
                    workSwitch.isChecked = true
                }

                // Get home category status
                @Suppress("RedundantIf")
                if (homeCat == "OFF"){
                    homeSwitch.isChecked = false
                }
                else{
                    homeSwitch.isChecked = true
                }

                // Get taxi category status
                @Suppress("RedundantIf")
                if (taxiCat == "OFF"){
                    taxiSwitch.isChecked = false
                }
                else{
                    taxiSwitch.isChecked = true
                }
            } else {
                Log.d("TAG", task.exception!!.message!!) //Don't ignore potential errors!
            }
        }
    }
}