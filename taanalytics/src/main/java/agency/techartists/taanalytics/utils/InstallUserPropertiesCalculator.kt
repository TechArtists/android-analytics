/*
 * Copyright (c) 2025 Tech Artists Agency SRL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package agency.techartists.taanalytics.utils

import agency.techartists.taanalytics.constants.UserProperties
import agency.techartists.taanalytics.core.TAAnalytics
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Calculates and sets user properties at install time (first open).
 */
class InstallUserPropertiesCalculator(
    private val context: Context,
    private val analytics: TAAnalytics,
    private val userPropertiesToCalculate: List<UserPropertyAnalyticsModel>
) {
    companion object {
        private const val TAG = "TAAnalytics.InstallProps"
    }

    /**
     * Calculate and set all configured install-time user properties.
     */
    fun calculateAndSetUserProperties() {
        userPropertiesToCalculate.forEach { property ->
            try {
                when (property) {
                    UserProperties.INSTALL_DATE -> setInstallDate()
                    UserProperties.INSTALL_VERSION -> setInstallVersion()
                    UserProperties.INSTALL_OS_VERSION -> setInstallOSVersion()
                    UserProperties.INSTALL_IS_ROOTED -> setInstallIsRooted()
                    UserProperties.INSTALL_UI_APPEARANCE -> setInstallUIAppearance()
                    else -> Log.w(TAG, "Unknown install user property: ${property.rawValue}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting property ${property.rawValue}: ${e.message}", e)
            }
        }
    }

    private fun setInstallDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val installDate = dateFormat.format(Date())
        analytics.set(UserProperties.INSTALL_DATE, installDate)
        Log.d(TAG, "Install date: $installDate")
    }

    private fun setInstallVersion() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = packageInfo.versionName ?: "unknown"
            analytics.set(UserProperties.INSTALL_VERSION, version)
            Log.d(TAG, "Install version: $version")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting install version: ${e.message}", e)
        }
    }

    private fun setInstallOSVersion() {
        val osVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        analytics.set(UserProperties.INSTALL_OS_VERSION, osVersion)
        Log.d(TAG, "Install OS version: $osVersion")
    }

    private fun setInstallIsRooted() {
        val isRooted = isDeviceRooted()
        analytics.set(UserProperties.INSTALL_IS_ROOTED, isRooted.toString())
        Log.d(TAG, "Install is rooted: $isRooted")
    }

    private fun setInstallUIAppearance() {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val appearance = when (uiMode) {
            Configuration.UI_MODE_NIGHT_YES -> "dark"
            Configuration.UI_MODE_NIGHT_NO -> "light"
            else -> "unknown"
        }
        analytics.set(UserProperties.INSTALL_UI_APPEARANCE, appearance)
        Log.d(TAG, "Install UI appearance: $appearance")
    }

    /**
     * Check if device is rooted by looking for common root indicators.
     * Note: This is not foolproof and can be bypassed.
     */
    private fun isDeviceRooted(): Boolean {
        // Check for su binary
        val suPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )

        for (path in suPaths) {
            if (File(path).exists()) {
                return true
            }
        }

        // Check for test-keys build
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        return false
    }
}
