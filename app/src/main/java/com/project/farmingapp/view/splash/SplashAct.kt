package com.project.farmingapp.view.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.project.farmingapp.databinding.ActivitySplashBinding
import com.project.farmingapp.view.auth.PhoneAuthActivity
import com.project.farmingapp.view.dashboard.DashboardActivity


class SplashAct : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    private val SPLASH_TIME_OUT: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // To start the animation
        binding.animationView.playAnimation()

        checkUserAndSendIntent()
    }

    private fun checkUserAndSendIntent() {
        if (auth.currentUser != null) {
            Handler().postDelayed({
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            }, SPLASH_TIME_OUT)
        } else {
            // Using a Handler to delay the start of the MainActivity
            Handler().postDelayed({
                val intent = Intent(this@SplashAct, PhoneAuthActivity::class.java)
                startActivity(intent)
                finish()
            }, SPLASH_TIME_OUT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to call cancelAnimation() in onDestroy to release resources
        binding.animationView.cancelAnimation()
    }
}
