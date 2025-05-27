package com.snowdaycalculator.snowcall

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.color.DynamicColors

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var initialColorInjectionDone = false
    private val TAG = "MainActivity"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply dynamic colors if available (Material You)
        // This must be called before setContentView
        if (DynamicColors.isDynamicColorAvailable()) {
            Log.d(TAG, "Applying dynamic colors to activity")
            DynamicColors.applyToActivityIfAvailable(this)
        } else {
            Log.d(TAG, "Dynamic colors not available on this device")
        }

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
        setupWebView()

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

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
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
        val webAppInterface = WebAppInterface(this)
        webView.addJavascriptInterface(webAppInterface, "Android")

        // Enhanced WebViewClient with detailed error handling
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "WebView page finished loading: $url")

                // When the page finishes loading, inject the system colors
                injectSystemColors()

                swipeRefreshLayout.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                val errorMessage = "Network error: ${error?.description}"
                Log.e(TAG, errorMessage)
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
                Log.e(TAG, "HTTP Error: ${errorResponse?.statusCode} for ${request?.url}")
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
                    Log.d(
                        "WebView Console",
                        "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}"
                    )
                }
                return true
            }
        }

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // Reset the flag when manually refreshing
            initialColorInjectionDone = false
            webView.reload()
        }
    }

    private fun injectSystemColors() {
        try {
            // Create an instance of WebAppInterface to get the system colors
            val webAppInterface = WebAppInterface(this)
            val colorJson = webAppInterface.getSystemColors()

            Log.d(TAG, "Injecting system colors into WebView")

            // Inject the colors into the WebView
            val script = """
                try {
                    const colors = $colorJson;
                    console.log('Received system colors from Android:', colors);
                    if (window.applySystemColors) {
                        window.applySystemColors(colors);
                        console.log('Applied system colors directly');
                    } else {
                        // If the function doesn't exist yet, wait and try again
                        console.log('applySystemColors not found, setting up retry');
                        window.systemColors = colors;
                        window.systemColorsReady = true;
                        
                        // Try again after a delay
                        setTimeout(() => {
                            if (window.applySystemColors) {
                                window.applySystemColors(colors);
                                console.log('Applied system colors after delay');
                            } else {
                                console.error('applySystemColors function not found after waiting');
                                // Try one more time after a longer delay
                                setTimeout(() => {
                                    if (window.applySystemColors) {
                                        window.applySystemColors(colors);
                                        console.log('Applied system colors after longer delay');
                                    }
                                }, 2000);
                            }
                        }, 1000);
                    }
                } catch (e) {
                    console.error('Error applying system colors:', e);
                }
            """.trimIndent()

            webView.evaluateJavascript(script) { result ->
                Log.d(TAG, "Color injection result: $result")
                initialColorInjectionDone = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error injecting colors", e)
        }
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

    override fun onResume() {
        super.onResume()

        // Re-apply colors when the activity resumes
        // This handles cases where system colors might have changed while the app was in background
        if (initialColorInjectionDone) {
            Log.d(TAG, "Activity resumed, re-injecting colors")
            injectSystemColors()
        }
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