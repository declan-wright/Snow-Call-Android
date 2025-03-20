package com.snowdaycalculator.snowcall

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.color.DynamicColors

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply dynamic colors if available (Material You)
        DynamicColors.applyToActivityIfAvailable(this)

        // Set up edge-to-edge display
        setupEdgeToEdgeDisplay()

        setContentView(R.layout.activity_main)

        // Initialize views
        webView = findViewById(R.id.webView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // Configure SwipeRefreshLayout colors
        swipeRefreshLayout.setColorSchemeResources(R.color.primary)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.TRANSPARENT)

        // Configure WebView with enhanced settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setGeolocationEnabled(true)
            loadWithOverviewMode = true
            useWideViewPort = true

            // Enhanced settings for network access
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT // Use default caching for better performance
            allowContentAccess = true
            allowFileAccess = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true

            // Enable web fonts
            standardFontFamily = "Roboto, sans-serif"
        }

        // Add JavaScript interface for native features
        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        // Enhanced WebViewClient with detailed error handling
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                val errorMessage = "Network error: ${error?.description}"
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                swipeRefreshLayout.isRefreshing = false
            }

            // Allow any SSL certificates (only for testing)
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed() // Accept SSL certificates
            }

            // Debug HTTP errors
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "HTTP Error: ${errorResponse?.statusCode} for ${request?.url}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Set up WebChromeClient for JavaScript dialogs and console messages
        webView.webChromeClient = object : WebChromeClient() {
            // Log console messages
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    android.util.Log.d(
                        "WebView Console",
                        "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}"
                    )
                }
                return true
            }
        }

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }

        // Handle back button presses with the new OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })

        // Load HTML from assets
        loadSnowCalculator()
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

    private fun loadSnowCalculator() {
        webView.loadUrl("file:///android_asset/snow-calculator.html")
    }
}