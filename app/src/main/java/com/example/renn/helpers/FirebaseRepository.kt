package com.example.renn.helpers

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

open class FirebaseRepository {
    // Current instance
    fun getInstance() = FirebaseAuth.getInstance()

    /* -- CURRENT USER STUFF -- */
    // Current user
    fun currentUser() = FirebaseAuth.getInstance().currentUser

    // Current user UID
    fun currentUserUid() = FirebaseAuth.getInstance().currentUser?.uid

    // Current user Email
    fun currentUserEmail() = FirebaseAuth.getInstance().currentUser?.email

    // Check if user is signed in
    @SuppressLint("SetTextI18n")
    @Suppress("RedundantIf")
    fun isUserSignedIn(): Boolean{
        return if(currentUser() == null){
            false
        } else{
            true
        }
    }

    /* -- FIREBASE STUFF -- */
    // Database getReference()
    fun dbRef(path: String?) = FirebaseDatabase.getInstance().getReference(path!!)



}