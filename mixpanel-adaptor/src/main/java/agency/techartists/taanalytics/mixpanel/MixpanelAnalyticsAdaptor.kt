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

package agency.techartists.taanalytics.mixpanel

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
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

/**
 * Mixpanel Analytics adaptor for TAAnalytics.
 *
 * Implements user ID read/write and pseudo ID read capabilities.
 *
 * **Trimming:**
 * - Event names: 255 characters (Mixpanel limit)
 * - User property names: 255 characters (Mixpanel limit)
 * - Parameter keys: 255 characters (Mixpanel limit)
 * - String values: No explicit limit, but recommended to keep reasonable
 *
 * **Type conversion:**
 * - String parameters → String
 * - Int/Long parameters → Long
 * - Float/Double parameters → Double
 * - Boolean parameters → Boolean
 *
 * Usage:
 * ```
 * val mixpanelAPI = MixpanelAPI.getInstance(context, "YOUR_TOKEN", true)
 * val mixpanelAdaptor = MixpanelAnalyticsAdaptor(mixpanelAPI)
 * val config = TAAnalyticsConfig(
 *     analyticsVersion = "1.0",
 *     adaptors = listOf(mixpanelAdaptor),
 *     sharedPreferences = sharedPreferences
 * )
 * val analytics = TAAnalytics(context, config)
 * ```
 */
class MixpanelAnalyticsAdaptor(
    private val mixpanelAPI: MixpanelAPI
) : AnalyticsAdaptor,
    AnalyticsAdaptorWithReadWriteUserID,
    AnalyticsAdaptorWithReadOnlyUserPseudoID {

    companion object {
        private const val EVENT_NAME_MAX_LENGTH = 255
        private const val USER_PROPERTY_NAME_MAX_LENGTH = 255
        private const val PARAMETER_KEY_MAX_LENGTH = 255
    }

    override suspend fun startFor(
        installType: TAAnalyticsConfig.InstallType,
        sharedPreferences: SharedPreferences
    ) {
        // Mixpanel doesn't require explicit start
        // It's automatically initialized when the instance is created
    }

    override fun track(
        trimmedEvent: EventAnalyticsModelTrimmed,
        params: Map<String, AnalyticsBaseParameterValue>?
    ) {
        val properties = JSONObject()

        params?.forEach { (key, value) ->
            val trimmedKey = key.take(PARAMETER_KEY_MAX_LENGTH)
            val actualValue = value.toAnalyticsValue()

            when (actualValue) {
                is String -> properties.put(trimmedKey, actualValue)
                is Int -> properties.put(trimmedKey, actualValue)
                is Long -> properties.put(trimmedKey, actualValue)
                is Float -> properties.put(trimmedKey, actualValue.toDouble())
                is Double -> properties.put(trimmedKey, actualValue)
                is Boolean -> properties.put(trimmedKey, actualValue)
                else -> properties.put(trimmedKey, actualValue.toString())
            }
        }

        mixpanelAPI.track(trimmedEvent.rawValue, properties)
    }

    override fun set(
        trimmedUserProperty: UserPropertyAnalyticsModelTrimmed,
        value: String?
    ) {
        val people = mixpanelAPI.people

        if (value != null) {
            people.set(trimmedUserProperty.rawValue, value)
        } else {
            // Setting to null removes the property
            people.unset(trimmedUserProperty.rawValue)
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
        if (userID != null) {
            mixpanelAPI.identify(userID)
            mixpanelAPI.people.identify(userID)
        } else {
            // Reset to anonymous tracking
            mixpanelAPI.reset()
        }
    }

    override fun getUserID(): String? {
        // Mixpanel doesn't provide a direct way to read the user ID back
        // We can get the distinct ID, but it might be the anonymous ID
        return mixpanelAPI.distinctId?.takeIf { it.isNotEmpty() }
    }

    override fun getUserPseudoID(): String? {
        // Return Mixpanel's distinct ID (which is the anonymous ID or user ID)
        return mixpanelAPI.distinctId
    }
}
