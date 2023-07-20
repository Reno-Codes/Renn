package com.example.renn.fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.renn.R
import com.example.renn.profile.ProfileActivity
import com.example.renn.settings.SettingsActivity
import com.example.renn.utils.auth
import com.example.renn.utils.database
import com.example.renn.utils.isUserSignedIn
import com.example.renn.utils.playLoadingAnimation
import com.example.renn.view_models.CurrentUserProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // View Models
    private lateinit var userViewModel: CurrentUserProfileViewModel

    // Animation
    private lateinit var flLottieAnimation: FrameLayout
    private lateinit var animationView: LottieAnimationView

    // Buttons, textViews etc.
    private lateinit var setLocationButton: Button
    private lateinit var civProfileSettings: CircleImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvCity: TextView

    // Start working btn
    private lateinit var btnStartWorking: Button
    private lateinit var flStartWorking: FrameLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val workEnabledRef = database
            .child("Users")
            .child(auth.currentUser!!.uid)
            .child("workEnabled")

        workEnabledRef.get().addOnCompleteListener {
            if (it.isSuccessful){
                val snapshot = it.result
                val isWorkEnabled = snapshot.getValue(Boolean::class.java)
                btnStartWorking = view.findViewById(R.id.btnStartWorking)
                flStartWorking = view.findViewById(R.id.flStartWorking)

                if (isWorkEnabled!!){
                    flStartWorking.visibility = View.GONE
                    // Animation FrameLayout and LottieAnimationView
                    flLottieAnimation = view.findViewById(R.id.flLottieAnimation)
                    animationView = view.findViewById(R.id.animationView)
                    playLoadingAnimation(flLottieAnimation, animationView)

                    setLocationButton = view.findViewById(R.id.setLocationButton)
                    civProfileSettings = view.findViewById(R.id.civProfileSettings)

                    tvUsername = view.findViewById(R.id.tvUsername)
                    tvCity = view.findViewById(R.id.tvCity)

                    if (isUserSignedIn()){
                        val currentUserIdRef = database
                            .child("Users")
                            .child(auth.currentUser!!.uid)

                        currentUserIdRef.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Get user's location LatLng from db
                                val snapshot1 = task.result
                                val country = snapshot1.child("Location_details").child("userCity")
                                    .getValue(String::class.java)
                                tvCity.text = country

                                // Stop animation
                                flLottieAnimation.visibility = View.GONE
                                animationView.cancelAnimation()
                            }
                        }

                        val currentUserEmail = auth.currentUser?.email
                        tvUsername.text = currentUserEmail
                    }

                    civProfileSettings.setOnClickListener {
                        val intent = Intent(context, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                    setLocationButton.setOnClickListener {
                        val intent = Intent(context, ProfileActivity::class.java)
                        startActivity(intent)
                    }

                }
                else {
                    flStartWorking.visibility = View.VISIBLE
                    //findNavController().navigate(R.id.enableWorkFragment)
                    btnStartWorking.setOnClickListener {
                        val intent = Intent(context, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }



        // Inflate the layout for this fragment
        return view
    }
}