package com.unchain.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.unchain.R
import com.unchain.auth.LoginActivity
import com.unchain.data.preferences.PreferencesManager

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        auth = Firebase.auth
        preferencesManager = PreferencesManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            checkNavigation()
        }, 2000)
    }

    private fun checkNavigation() {
        when {
            //klo user udah login, langsung ke main
            auth.currentUser != null -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            //klo pertama kali ke onboarding
            preferencesManager.isFirstTime() -> {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            //selain itu ke login
            else -> {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        finish()
    }
}