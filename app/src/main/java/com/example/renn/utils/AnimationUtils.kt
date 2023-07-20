package com.example.renn.utils

import android.animation.Animator
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.example.renn.R

class Animation{
    lateinit var layout: FrameLayout
    lateinit var animationView: LottieAnimationView


}

fun playLoadingAnimation(layout: FrameLayout, animationView: LottieAnimationView){
    // Loading animation
    layout.visibility = View.VISIBLE
    animationView.setAnimation(R.raw.loading_three_dots_red_white)
    animationView.repeatCount = 999
    animationView.playAnimation()


    animationView.addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            Log.e("Animation:", "start")
        }
        override fun onAnimationEnd(animation: Animator) {
            Log.e("Animation:", "end")
            //Ex: here the layout is removed!

            if (!animation.isRunning){
                layout.visibility= View.GONE
            }
        }

        override fun onAnimationCancel(animation: Animator) {
            Log.e("Animation:", "cancel")
            if (!animation.isRunning){
                layout.visibility= View.GONE
            }
        }

        override fun onAnimationRepeat(animation: Animator) {
            Log.e("Animation:", "repeat")
        }

    })
}


fun playSuccessAnimation(layout: FrameLayout, animationView: LottieAnimationView){
    // Loading animation
    layout.visibility = View.VISIBLE
    animationView.setAnimation(R.raw.success_checked_green)
    animationView.repeatCount = 0
    animationView.playAnimation()


    animationView.addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            Log.e("Animation:", "start")
        }
        override fun onAnimationEnd(animation: Animator) {
            Log.e("Animation:", "end")
            //Ex: here the layout is removed!

            if (!animation.isRunning){
                layout.visibility= View.GONE
            }
        }

        override fun onAnimationCancel(animation: Animator) {
            Log.e("Animation:", "cancel")
        }

        override fun onAnimationRepeat(animation: Animator) {
            Log.e("Animation:", "repeat")
        }

    })
}



fun playFailedAnimation(layout: FrameLayout, animationView: LottieAnimationView){
    // Loading animation
    layout.visibility = View.VISIBLE
    animationView.setAnimation(R.raw.error_animation)
    animationView.repeatCount = 0
    animationView.playAnimation()


    animationView.addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            Log.e("Animation:", "start")
        }
        override fun onAnimationEnd(animation: Animator) {
            Log.e("Animation:", "end")
            //Ex: here the layout is removed!

            if (!animation.isRunning){
                layout.visibility= View.GONE
            }
        }

        override fun onAnimationCancel(animation: Animator) {
            Log.e("Animation:", "cancel")
        }

        override fun onAnimationRepeat(animation: Animator) {
            Log.e("Animation:", "repeat")
        }

    })
}