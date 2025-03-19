package com.snowdaycalculator.snowcall

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        webView = findViewById(R.id.webView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // Configure WebView with enhanced settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setGeolocationEnabled(true)
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true

            // Enhanced settings for network access
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE // Try with no cache
            allowContentAccess = true
            allowFileAccess = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true
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

        // Load HTML from assets
        loadSnowCalculator()
    }

    private fun loadSnowCalculator() {
        webView.loadUrl("file:///android_asset/snow-calculator.html")
    }

    // Handle back button presses
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}