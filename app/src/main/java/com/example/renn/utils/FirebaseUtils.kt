package com.example.renn.utils

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


val auth : FirebaseAuth = FirebaseAuth.getInstance()
val database: DatabaseReference = FirebaseDatabase.getInstance().reference


// Check if user is signed in
@SuppressLint("SetTextI18n")
@Suppress("RedundantIf")
fun isUserSignedIn(): Boolean{
    return if(FirebaseAuth.getInstance().currentUser != null){
        true
    } else{
        false
    }
}

// Database getReference()
fun dbRef(path: String?) = FirebaseDatabase.getInstance().getReference(path!!)


