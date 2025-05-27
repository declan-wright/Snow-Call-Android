package com.snowdaycalculator.snowcall

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import org.json.JSONObject

class WebAppInterface(private val context: Context) {

    private val TAG = "WebAppInterface"

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun shareContent(title: String, text: String, url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$text $url")
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    @JavascriptInterface
    fun vibrate(milliseconds: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(milliseconds.toLong(), android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                vibrator.vibrate(milliseconds.toLong())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating", e)
        }
    }

    @JavascriptInterface
    fun vibrate(pattern: String) {
        try {
            val patternArray = pattern.split(",").map { it.trim().toLong() }.toLongArray()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(patternArray, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                vibrator.vibrate(patternArray, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating with pattern", e)
        }
    }

    @JavascriptInterface
    fun isDarkMode(): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    @JavascriptInterface
    fun setDarkMode(isDark: Boolean) {
        // This is a stub method - actual implementation would require AppCompatDelegate
        // But we'll respond to the JavaScript call to keep the API consistent
        Toast.makeText(context, "Dark mode ${if (isDark) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun getSystemColors(): String {
        val colorJson = JSONObject()
        Log.d(TAG, "Getting system colors. Dynamic colors available: ${DynamicColors.isDynamicColorAvailable()}")

        try {
            // Try to use a dynamically themed context if available
            val contextToUse = if (context is androidx.appcompat.app.AppCompatActivity) {
                // This will give us a context with dynamic colors applied
                context
            } else {
                // Fallback to the original context
                context
            }

            // Check if we're running on a device with dynamic colors support
            if (DynamicColors.isDynamicColorAvailable()) {
                Log.d(TAG, "Using Material You dynamic colors")

                // Material 3 Primary Colors
                try {
                    val primaryColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorPrimary, Color.BLACK)
                    colorJson.put("primary", colorToHex(primaryColor))
                    Log.d(TAG, "Primary color: ${colorToHex(primaryColor)}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting primary color", e)
                    colorJson.put("primary", colorToHex(ContextCompat.getColor(context, R.color.primary)))
                }

                try {
                    val onPrimaryColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorOnPrimary, Color.WHITE)
                    colorJson.put("onPrimary", colorToHex(onPrimaryColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting onPrimary color", e)
                    colorJson.put("onPrimary", "#FFFFFF")
                }

                try {
                    val primaryContainerColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorPrimaryContainer, Color.LTGRAY)
                    colorJson.put("primaryContainer", colorToHex(primaryContainerColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting primaryContainer color", e)
                    colorJson.put("primaryContainer", colorToHex(ContextCompat.getColor(context, R.color.primary_variant)))
                }

                try {
                    val onPrimaryContainerColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.BLACK)
                    colorJson.put("onPrimaryContainer", colorToHex(onPrimaryContainerColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting onPrimaryContainer color", e)
                    colorJson.put("onPrimaryContainer", "#FFFFFF")
                }

                // Material 3 Secondary Colors
                try {
                    val secondaryColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorSecondary, Color.GRAY)
                    colorJson.put("secondary", colorToHex(secondaryColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting secondary color", e)
                    colorJson.put("secondary", colorToHex(ContextCompat.getColor(context, R.color.secondary)))
                }

                try {
                    val onSecondaryColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorOnSecondary, Color.WHITE)
                    colorJson.put("onSecondary", colorToHex(onSecondaryColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting onSecondary color", e)
                    colorJson.put("onSecondary", "#FFFFFF")
                }

                try {
                    val secondaryContainerColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorSecondaryContainer, Color.LTGRAY)
                    colorJson.put("secondaryContainer", colorToHex(secondaryContainerColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting secondaryContainer color", e)
                    colorJson.put("secondaryContainer", colorToHex(ContextCompat.getColor(context, R.color.secondary_variant)))
                }

                try {
                    val onSecondaryContainerColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorOnSecondaryContainer, Color.BLACK)
                    colorJson.put("onSecondaryContainer", colorToHex(onSecondaryContainerColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting onSecondaryContainer color", e)
                    colorJson.put("onSecondaryContainer", "#FFFFFF")
                }

                // Material 3 Tertiary Colors
                try {
                    val tertiaryColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorTertiary, Color.GRAY)
                    colorJson.put("tertiary", colorToHex(tertiaryColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting tertiary color", e)
                    colorJson.put("tertiary", colorToHex(ContextCompat.getColor(context, R.color.primary)))
                }

                try {
                    val onTertiaryColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorOnTertiary, Color.WHITE)
                    colorJson.put("onTertiary", colorToHex(onTertiaryColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting onTertiary color", e)
                    colorJson.put("onTertiary", "#FFFFFF")
                }

                // Material 3 Surface Colors
                try {
                    val surfaceColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorSurface, if (isDarkMode()) Color.parseColor("#272726") else Color.WHITE)
                    colorJson.put("surface", colorToHex(surfaceColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting surface color", e)
                    colorJson.put("surface", if (isDarkMode()) "#272726" else "#FFFFFF")
                }

                try {
                    val onSurfaceColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorOnSurface, if (isDarkMode()) Color.WHITE else Color.BLACK)
                    colorJson.put("onSurface", colorToHex(onSurfaceColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting onSurface color", e)
                    colorJson.put("onSurface", colorToHex(ContextCompat.getColor(context, R.color.text_primary)))
                }

                try {
                    val surfaceVariantColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorSurfaceVariant, Color.LTGRAY)
                    colorJson.put("surfaceVariant", colorToHex(surfaceVariantColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting surfaceVariant color", e)
                    colorJson.put("surfaceVariant", colorToHex(ContextCompat.getColor(context, R.color.background_light)))
                }

                try {
                    val onSurfaceVariantColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.DKGRAY)
                    colorJson.put("onSurfaceVariant", colorToHex(onSurfaceVariantColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting onSurfaceVariant color", e)
                    colorJson.put("onSurfaceVariant", colorToHex(ContextCompat.getColor(context, R.color.text_secondary)))
                }

                // Other Material 3 Colors
                try {
                    val outlineColor = MaterialColors.getColor(contextToUse, com.google.android.material.R.attr.colorOutline, Color.GRAY)
                    colorJson.put("outline", colorToHex(outlineColor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting outline color", e)
                    colorJson.put("outline", "#79747E")
                }

                // Try to get surfaceContainer color - might not be available on all Material 3 versions
                try {
                    colorJson.put("surfaceContainer", if (isDarkMode()) "#3e3d3a" else "#F5F5F5")

                    // On newer versions, we might have more specific container colors
                    try {
                        val surfaceContainerColor = MaterialColors.getColor(contextToUse,
                            com.google.android.material.R.attr.colorSurfaceContainerLow, Color.TRANSPARENT)
                        if (surfaceContainerColor != Color.TRANSPARENT) {
                            colorJson.put("surfaceContainer", colorToHex(surfaceContainerColor))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting surfaceContainer color", e)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting surfaceContainer color", e)
                }

            } else {
                Log.d(TAG, "Dynamic colors not available, using fallback colors")
                // If Material You is not available, use app's theme colors
                colorJson.put("primary", colorToHex(ContextCompat.getColor(context, R.color.primary)))
                colorJson.put("onPrimary", "#FFFFFF")
                colorJson.put("primaryContainer", colorToHex(ContextCompat.getColor(context, R.color.primary_variant)))
                colorJson.put("onPrimaryContainer", "#FFFFFF")

                colorJson.put("secondary", colorToHex(ContextCompat.getColor(context, R.color.secondary)))
                colorJson.put("onSecondary", "#FFFFFF")
                colorJson.put("secondaryContainer", colorToHex(ContextCompat.getColor(context, R.color.secondary_variant)))
                colorJson.put("onSecondaryContainer", "#FFFFFF")

                // Default tertiary color
                colorJson.put("tertiary", colorToHex(ContextCompat.getColor(context, R.color.primary)))
                colorJson.put("onTertiary", "#FFFFFF")

                // Surface colors
                colorJson.put("surface", if (isDarkMode()) "#272726" else "#FFFFFF")
                colorJson.put("onSurface", colorToHex(ContextCompat.getColor(context, R.color.text_primary)))
                colorJson.put("surfaceVariant", colorToHex(ContextCompat.getColor(context, R.color.background_light)))
                colorJson.put("onSurfaceVariant", colorToHex(ContextCompat.getColor(context, R.color.text_secondary)))

                // Other colors
                colorJson.put("outline", "#79747E")
                colorJson.put("surfaceContainer", if (isDarkMode()) "#3e3d3a" else "#F5F5F5")
            }

            Log.d(TAG, "Final color JSON: ${colorJson.toString()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating color JSON", e)
            e.printStackTrace()
        }

        return colorJson.toString()
    }

    private fun colorToHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
}