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

package agency.techartists.taanalytics.firebase

import agency.techartists.taanalytics.adaptor.AnalyticsAdaptor
import agency.techartists.taanalytics.adaptor.AnalyticsAdaptorWithReadOnlyUserPseudoID
import agency.techartists.taanalytics.adaptor.AnalyticsAdaptorWithReadWriteUserID
import agency.techartists.taanalytics.core.TAAnalyticsConfig
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.EventAnalyticsModelTrimmed
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModelTrimmed
import android.content.SharedPreferences
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Firebase Analytics adaptor for TAAnalytics.
 *
 * Implements user ID read/write and pseudo ID read capabilities.
 *
 * **Trimming:**
 * - Event names: 40 characters (Firebase limit)
 * - User property names: 24 characters (Firebase limit)
 * - Parameter keys: 40 characters (Firebase limit)
 * - String values: 100 characters (Firebase limit)
 *
 * **Type conversion:**
 * - String parameters → String
 * - Int/Long parameters → Long
 * - Float/Double parameters → Double
 * - Boolean parameters → String ("true"/"false")
 *
 * Usage:
 * ```
 * val firebaseAdaptor = FirebaseAnalyticsAdaptor(firebaseAnalytics)
 * val config = TAAnalyticsConfig(
 *     analyticsVersion = "1.0",
 *     adaptors = listOf(firebaseAdaptor),
 *     sharedPreferences = sharedPreferences
 * )
 * val analytics = TAAnalytics(context, config)
 * ```
 */
class FirebaseAnalyticsAdaptor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsAdaptor,
    AnalyticsAdaptorWithReadWriteUserID,
    AnalyticsAdaptorWithReadOnlyUserPseudoID {

    companion object {
        private const val EVENT_NAME_MAX_LENGTH = 40
        private const val USER_PROPERTY_NAME_MAX_LENGTH = 24
        private const val PARAMETER_KEY_MAX_LENGTH = 40
        private const val STRING_VALUE_MAX_LENGTH = 100
    }

    override suspend fun startFor(
        installType: TAAnalyticsConfig.InstallType,
        sharedPreferences: SharedPreferences
    ) {
        // Firebase Analytics doesn't require explicit start
        // It's automatically initialized by Firebase SDK
    }

    override fun track(
        trimmedEvent: EventAnalyticsModelTrimmed,
        params: Map<String, AnalyticsBaseParameterValue>?
    ) {
        val bundle = Bundle()

        params?.forEach { (key, value) ->
            val trimmedKey = key.take(PARAMETER_KEY_MAX_LENGTH)
            val actualValue = value.toAnalyticsValue()

            when (actualValue) {
                is String -> {
                    val trimmedValue = actualValue.take(STRING_VALUE_MAX_LENGTH)
                    bundle.putString(trimmedKey, trimmedValue)
                }
                is Int -> {
                    bundle.putLong(trimmedKey, actualValue.toLong())
                }
                is Long -> {
                    bundle.putLong(trimmedKey, actualValue)
                }
                is Float -> {
                    bundle.putDouble(trimmedKey, actualValue.toDouble())
                }
                is Double -> {
                    bundle.putDouble(trimmedKey, actualValue)
                }
                is Boolean -> {
                    // Firebase doesn't support boolean params directly
                    bundle.putString(trimmedKey, actualValue.toString())
                }
                else -> {
                    // Fallback to string representation
                    bundle.putString(trimmedKey, actualValue.toString().take(STRING_VALUE_MAX_LENGTH))
                }
            }
        }

        firebaseAnalytics.logEvent(trimmedEvent.rawValue, bundle)
    }

    override fun set(
        trimmedUserProperty: UserPropertyAnalyticsModelTrimmed,
        value: String?
    ) {
        if (value != null) {
            val trimmedValue = value.take(STRING_VALUE_MAX_LENGTH)
            firebaseAnalytics.setUserProperty(trimmedUserProperty.rawValue, trimmedValue)
        } else {
            // Setting to null removes the property
            firebaseAnalytics.setUserProperty(trimmedUserProperty.rawValue, null)
        }
    }

    override fun trim(event: EventAnalyticsModel): EventAnalyticsModelTrimmed {
        val trimmedValue = event.rawValue.take(EVENT_NAME_MAX_LENGTH)
        return EventAnalyticsModelTrimmed(trimmedValue)
    }

    override fun trim(userProperty: UserPropertyAnalyticsModel): UserPropertyAnalyticsModelTrimmed {
        val trimmedValue = userProperty.rawValue.take(USER_PROPERTY_NAME_MAX_LENGTH)
        return UserPropertyAnalyticsModelTrimmed(trimmedValue)
    }

    // MARK: - User ID Support

    override fun setUserID(userID: String?) {
        firebaseAnalytics.setUserId(userID)
    }

    override fun getUserID(): String? {
        // Firebase doesn't provide a way to read the user ID back
        // Return null to indicate read is not supported
        return null
    }

    override fun getUserPseudoID(): String? {
        // Firebase App Instance ID (synchronous)
        return firebaseAnalytics.appInstanceId.result
    }
}
