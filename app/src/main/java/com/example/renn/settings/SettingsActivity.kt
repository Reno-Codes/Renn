package com.example.renn.settings

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.example.renn.R
import com.example.renn.utils.*
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var homeSwitch: SwitchMaterial
    private lateinit var taxiSwitch: SwitchMaterial
    private lateinit var workSwitch: SwitchMaterial

    private lateinit var backBtn: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val currentUserId = auth.currentUser!!.uid
        val usersRef = database.child("Users")
        val currentUserIdRef = usersRef.child(currentUserId)

        homeSwitch = findViewById(R.id.switchHome)
        taxiSwitch = findViewById(R.id.switchTaxi)
        workSwitch = findViewById(R.id.switchWork)

        // Back button
        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }


        //val userEmail= FirebaseAuth.getInstance().currentUser!!.email
        checkCategories(currentUserId)

        // Enable/Disable work switch
        workSwitch.setOnCheckedChangeListener { _, _ ->
            if (workSwitch.isChecked) {
                // Enable Work for user in Database
                currentUserIdRef.child("workEnabled").setValue(true).addOnSuccessListener {
                    Log.d("workEnabled", "workEnabled: workEnabled = true")
                }.addOnFailureListener {
                    Log.d("workEnabled", "workEnabled: Can't enable work")
                }

                workSwitch.text = getString(R.string.disable_work)
                //workSwitch.setTextColor(Color.parseColor("#0aad3f"))
                homeSwitch.visibility = View.VISIBLE
                taxiSwitch.visibility = View.VISIBLE
                checkCategories(currentUserId)
            }
            else{
                // Disable Work for user in Database
                currentUserIdRef.child("workEnabled").setValue(false).addOnSuccessListener {
                    Log.d("workEnabled", "workEnabled: workEnabled = false")
                }.addOnFailureListener {
                    Log.d("workEnabled", "workEnabled: Can't disable work")
                }

                workSwitch.text = getString(R.string.enable_work)
                workSwitch.setTextColor(Color.parseColor("#FBFBFF"))
                homeSwitch.visibility = View.INVISIBLE
                taxiSwitch.visibility = View.INVISIBLE
                currentUserIdRef.child("Categories").child("homeCat").setValue(false)
                currentUserIdRef.child("Categories").child("taxiCat").setValue(false)
                checkCategories(currentUserId)
            }
        }

        // Home Category switch
        homeSwitch.setOnCheckedChangeListener { _, _ ->
            if (homeSwitch.isChecked) {
                currentUserIdRef.child("Categories").child("homeCat").setValue(true).addOnSuccessListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat = true")
                    // Add user to category sorted table
                    usersRef.child("All_Categories").child("HomeCategory").child("Workers").child(currentUserId).setValue(currentUserId)
                }.addOnFailureListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat can't be updated")
                }
            }
            else{
                currentUserIdRef.child("Categories").child("homeCat").setValue(false).addOnSuccessListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat = false")
                    // Remove user from category sorted table
                    usersRef.child("All_Categories").child("HomeCategory").child("Workers").child(currentUserId).setValue(null)
                }.addOnFailureListener {
                    Log.d("UpdateHomeCat", "updateHomeCat: homeCat can't be updated")
                }
            }
        }


        taxiSwitch.setOnCheckedChangeListener { _, _ ->
            if (taxiSwitch.isChecked) {
                currentUserIdRef.child("Categories").child("taxiCat").setValue(true).addOnSuccessListener {
                    Log.d("updateTaxiCat", "updateTaxiCat: taxiCat = true")
                    // Add user to category sorted table
                    usersRef.child("All_Categories").child("TaxiCategory").child("Workers").child(
                        currentUserId).setValue(currentUserId)
                }.addOnFailureListener {
                    Log.d("updateTaxiCat", "updateTaxiCat: taxiCat can't be updated")
                }
            }
            else{
                currentUserIdRef.child("Categories").child("taxiCat").setValue(false).addOnSuccessListener {
                    Log.d("updateTaxiCat", "updateTaxiCat: taxiCat = false")
                    // Remove user from category sorted table
                    usersRef.child("All_Categories").child("TaxiCategory").child("Workers").child(currentUserId).setValue(null)
                }.addOnFailureListener {
                    Log.d("updateTaxiCat", "updateTaxiCat: taxiCat can't be updated")
                }
            }
        }

    }

    private fun checkCategories(userid: String){
        val uidRef = database.child("Users").child(userid)
        uidRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get snapshot
                val snapshot = task.result
                val workStatus = snapshot.child("workEnabled").getValue(Boolean::class.java)
                val homeCat = snapshot.child("Categories").child("homeCat").getValue(Boolean::class.java)
                val taxiCat = snapshot.child("Categories").child("taxiCat").getValue(Boolean::class.java)

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
                if (homeCat == false){
                    homeSwitch.isChecked = false
                }
                else{
                    homeSwitch.isChecked = true
                }

                // Get taxi category status
                @Suppress("RedundantIf")
                if (taxiCat == false){
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