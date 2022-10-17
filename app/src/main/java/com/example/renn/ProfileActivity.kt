package com.example.renn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {
    //private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var homeSwitch: SwitchMaterial
    private lateinit var taxiSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(binding.root)
        setContentView(R.layout.activity_profile)

        homeSwitch = findViewById(R.id.switchHome)
        taxiSwitch = findViewById(R.id.switchTaxi)

        database = FirebaseDatabase.getInstance().getReference("Users")
        val userid= FirebaseAuth.getInstance().currentUser!!.uid
        //val userEmail= FirebaseAuth.getInstance().currentUser!!.email



        homeSwitch.setOnCheckedChangeListener { _, _ ->
            if (homeSwitch.isChecked) {
                database.child(userid).child("homeCat").setValue("ON").addOnSuccessListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat = ON!")
                    database.child("HomeCategory").child(userid).setValue(userid)
                }.addOnFailureListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat can't be updated")
                }
            }
            else{
                database.child(userid).child("homeCat").setValue("OFF").addOnSuccessListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat = OFF!")
                    database.child("HomeCategory").child(userid).setValue(null)
                }.addOnFailureListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat can't be updated")
                }
            }
        }


        taxiSwitch.setOnCheckedChangeListener { _, _ ->
            if (taxiSwitch.isChecked) {
                database.child(userid).child("taxiCat").setValue("ON").addOnSuccessListener {
                    Log.d("UpdatetaxiCat", "updatetaxiCat: taxiCat = ON!")
                    database.child("TaxiCategory").child(userid).setValue(userid)
                }.addOnFailureListener {
                    Log.d("UpdatetaxiCat", "updatetaxiCat: taxiCat can't be updated")
                }
            }
            else{
                database.child(userid).child("taxiCat").setValue("OFF").addOnSuccessListener {
                    Log.d("UpdatetaxiCat", "updatetaxiCat: taxiCat = OFF!")
                    database.child("TaxiCategory").child(userid).setValue(null)
                }.addOnFailureListener {
                    Log.d("UpdatetaxiCat", "updatetaxiCat: taxiCat can't be updated")
                }
            }
        }


        val uidRef = database.child(userid)
        uidRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val homeCat = snapshot.child("homeCat").getValue(String::class.java)
                val taxiCat = snapshot.child("taxiCat").getValue(String::class.java)

                @Suppress("RedundantIf")
                if (homeCat == "OFF"){
                    homeSwitch.isChecked = false
                }
                else{
                    homeSwitch.isChecked = true
                }
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