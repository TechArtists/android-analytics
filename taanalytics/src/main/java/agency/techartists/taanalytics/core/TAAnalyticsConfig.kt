//  TAAnalyticsConfig.kt
//
//  Copyright (c) 2024 Tech Artists Agency SRL
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
//

package agency.techartists.taanalytics.core

import agency.techartists.taanalytics.adaptor.AnalyticsAdaptor
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import android.content.Context
import android.content.SharedPreferences

/**
 * Configuration for TAAnalytics.
 *
 * @param analyticsVersion Separate user property that tracks the version of the analytics events.
 *                         Ideally, when you add/modify an event, this version would also be changed
 *                         and communicated to the BI team.
 * @param adaptors List of analytics adaptors to use (Firebase, Amplitude, etc.)
 * @param sharedPreferences SharedPreferences instance to use for persistence
 * @param automaticallyTrackedEventsPrefixConfig Prefix for events/user properties automatically
 *                                                tracked by this internal library
 * @param manuallyTrackedEventsPrefixConfig Prefix for events/user properties sent manually by
 *                                          your app via track() or set()
 * @param trackEventFilter Filter function to selectively send events
 */
data class TAAnalyticsConfig(
    val analyticsVersion: String,
    val adaptors: List<AnalyticsAdaptor>,
    val sharedPreferences: SharedPreferences,
    val automaticallyTrackedEventsPrefixConfig: PrefixConfig = PrefixConfig("", ""),
    val manuallyTrackedEventsPrefixConfig: PrefixConfig = PrefixConfig("", ""),
    val trackEventFilter: (EventAnalyticsModel, Map<String, AnalyticsBaseParameterValue>?) -> Boolean = { _, _ -> true }
) {
    /**
     * Type of installation/build.
     */
    enum class InstallType {
        /** Installed from Google Play Store */
        PLAY_STORE,

        /** Debug build from Android Studio */
        DEBUG,

        /** Release build but not from Play Store (e.g., side-loaded APK) */
        RELEASE
    }

    /**
     * Configuration for event and user property name prefixes.
     *
     * @param eventPrefix Prefix to add to all event names
     * @param userPropertyPrefix Prefix to add to all user property names
     */
    data class PrefixConfig(
        val eventPrefix: String,
        val userPropertyPrefix: String
    )

    companion object {
        /**
         * Determines the install type based on build configuration.
         *
         * @param context Android context
         * @return The detected install type
         */
        fun findInstallType(context: Context): InstallType {
            return try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val installer = context.packageManager.getInstallerPackageName(context.packageName)

                when {
                    installer == "com.android.vending" -> InstallType.PLAY_STORE
                    (packageInfo.applicationInfo?.flags?.and(android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) ?: 0) != 0 -> InstallType.DEBUG
                    else -> InstallType.RELEASE
                }
            } catch (e: Exception) {
                InstallType.DEBUG
            }
        }
    }
}
