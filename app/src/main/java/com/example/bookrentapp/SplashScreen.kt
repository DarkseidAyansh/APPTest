package com.example.bookrentapp

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.bookrentapp.databinding.ActivitySplashScreenBinding


class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the logo animation
        val logoAnimator = ObjectAnimator.ofFloat(binding.logo, "alpha", 0f, 1f)
        logoAnimator.duration = 1500
        logoAnimator.interpolator = AccelerateDecelerateInterpolator()

        // Initialize the app name and tagline animations
        val appNameAnimator = ObjectAnimator.ofFloat(binding.appName, "translationY", 500f, 0f)
        appNameAnimator.duration = 1200
        appNameAnimator.startDelay = 1500
        appNameAnimator.interpolator = AccelerateDecelerateInterpolator()

        val appTaglineAnimator = ObjectAnimator.ofFloat(binding.appTagline, "translationY", 500f, 0f)
        appTaglineAnimator.duration = 1200
        appTaglineAnimator.startDelay = 1800
        appTaglineAnimator.interpolator = AccelerateDecelerateInterpolator()

        // Start the animations
        logoAnimator.start()
        appNameAnimator.start()
        appTaglineAnimator.start()

        // After animation ends, move to MainActivity
        appTaglineAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                binding.appName.visibility = View.VISIBLE
                binding.appTagline.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                // Wait for the animation to finish and then navigate to MainActivity
                val intent = Intent(this@SplashScreen, Login::class.java)
                startActivity(intent)
                finish()  // Close SplashScreen activity
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }
}
