package com.example.renn.view_models

import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.airbnb.lottie.LottieAnimationView
import com.example.renn.utils.auth
import com.example.renn.utils.database
import com.example.renn.utils.playLoadingAnimation

class CurrentUserProfileViewModel : ViewModel() {
    var userEmail = MutableLiveData<String>()
    var userCity = MutableLiveData<String>()

    init {
        userEmail.value = "email"
        userCity.value = "city"
    }


    fun updateWorkerInfo(flLottieAnimation: FrameLayout, animationView: LottieAnimationView){
        // Play loading animation
        playLoadingAnimation(flLottieAnimation, animationView)

        val userIdReference = database
            .child("Users")
            .child(auth.currentUser!!.uid)

        userIdReference.get().addOnCompleteListener {
            if (it.isSuccessful){
                val snapshot = it.result
                userEmail.value = snapshot.child("email").getValue(String::class.java)!!
                userCity.value = snapshot.child("Location_details").child("userCity").getValue(String::class.java)!!

                // Stop animation
                flLottieAnimation.visibility = View.GONE
                animationView.cancelAnimation()
            }
        }
    }

}