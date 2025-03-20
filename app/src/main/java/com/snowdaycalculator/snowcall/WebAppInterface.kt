package com.snowdaycalculator.snowcall

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import org.json.JSONObject

class WebAppInterface(private val context: Context) {

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
            e.printStackTrace()
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
            e.printStackTrace()
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

        try {
            if (DynamicColors.isDynamicColorAvailable()) {
                // Use Material You dynamic colors
                val resources = context.resources
                val theme = context.theme

                // Primary colors
                colorJson.put("primary", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, Color.BLACK)))
                colorJson.put("onPrimary", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimary, Color.WHITE)))
                colorJson.put("primaryContainer", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimaryContainer, Color.LTGRAY)))
                colorJson.put("onPrimaryContainer", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.DKGRAY)))

                // Secondary colors
                colorJson.put("secondary", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondary, Color.GRAY)))
                colorJson.put("onSecondary", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondary, Color.WHITE)))
                colorJson.put("secondaryContainer", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondaryContainer, Color.LTGRAY)))
                colorJson.put("onSecondaryContainer", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondaryContainer, Color.DKGRAY)))

                // Tertiary colors
                colorJson.put("tertiary", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorTertiary, Color.GRAY)))
                colorJson.put("onTertiary", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnTertiary, Color.WHITE)))

                // Surface colors
                colorJson.put("surface", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.WHITE)))
                colorJson.put("onSurface", colorToHex(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK)))
            } else {
                // Use default theme colors instead of dynamic ones
                colorJson.put("primary", colorToHex(ContextCompat.getColor(context, R.color.primary)))
                colorJson.put("onPrimary", "#FFFFFF")
                colorJson.put("primaryContainer", colorToHex(ContextCompat.getColor(context, R.color.background_light)))
                colorJson.put("onPrimaryContainer", colorToHex(ContextCompat.getColor(context, R.color.text_primary)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return colorJson.toString()
    }

    private fun colorToHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
}