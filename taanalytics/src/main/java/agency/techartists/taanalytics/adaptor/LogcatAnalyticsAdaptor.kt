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

package agency.techartists.taanalytics.adaptor

import agency.techartists.taanalytics.core.TAAnalyticsConfig
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.EventAnalyticsModelTrimmed
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModelTrimmed
import android.content.SharedPreferences
import android.util.Log

/**
 * Analytics adaptor that logs all events and user properties to Android Logcat.
 * Useful for debugging and development.
 */
class LogcatAnalyticsAdaptor : AnalyticsAdaptor {

    companion object {
        private const val TAG = "TAAnalytics"
    }

    override suspend fun startFor(
        installType: TAAnalyticsConfig.InstallType,
        sharedPreferences: SharedPreferences
    ) {
        // No initialization needed for Logcat
        Log.i(TAG, "LogcatAnalyticsAdaptor started for install type: $installType")
    }

    override fun track(
        trimmedEvent: EventAnalyticsModelTrimmed,
        params: Map<String, AnalyticsBaseParameterValue>?
    ) {
        val paramsString = params
            ?.toList()
            ?.sortedBy { it.first }
            ?.joinToString(", ") { "${it.first}:${it.second}" }
            ?: ""

        if (paramsString.isNotEmpty()) {
            Log.i(TAG, "Event: '${trimmedEvent.rawValue}', params: [$paramsString]")
        } else {
            Log.i(TAG, "Event: '${trimmedEvent.rawValue}'")
        }
    }

    override fun set(
        trimmedUserProperty: UserPropertyAnalyticsModelTrimmed,
        value: String?
    ) {
        Log.i(TAG, "UserProperty: '${trimmedUserProperty.rawValue}' = '${value ?: "null"}'")
    }

    override fun trim(event: EventAnalyticsModel): EventAnalyticsModelTrimmed {
        // Trim to 40 characters (Firebase's limit)
        val trimmed = if (event.rawValue.length > 40) {
            Log.w(TAG, "Event name '${event.rawValue}' trimmed to 40 characters")
            event.rawValue.take(40)
        } else {
            event.rawValue
        }
        return EventAnalyticsModelTrimmed(trimmed)
    }

    override fun trim(userProperty: UserPropertyAnalyticsModel): UserPropertyAnalyticsModelTrimmed {
        // Trim to 24 characters (Firebase's limit)
        val trimmed = if (userProperty.rawValue.length > 24) {
            Log.w(TAG, "User property '${userProperty.rawValue}' trimmed to 24 characters")
            userProperty.rawValue.take(24)
        } else {
            userProperty.rawValue
        }
        return UserPropertyAnalyticsModelTrimmed(trimmed)
    }
}
