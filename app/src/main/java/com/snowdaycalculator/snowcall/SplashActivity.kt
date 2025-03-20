package com.snowdaycalculator.snowcall

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up edge-to-edge display
        setupEdgeToEdgeDisplay()

        setContentView(R.layout.activity_splash)

        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500) // 1.5 seconds
    }

    private fun setupEdgeToEdgeDisplay() {
        // Make the app draw under the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Get the WindowInsetsController using WindowCompat
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)

        // Configure the behavior of the system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Hide the system bars
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Make status bar and navigation bar transparent
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-apply immersive mode when the window gets focus
            setupEdgeToEdgeDisplay()
        }
    }
}