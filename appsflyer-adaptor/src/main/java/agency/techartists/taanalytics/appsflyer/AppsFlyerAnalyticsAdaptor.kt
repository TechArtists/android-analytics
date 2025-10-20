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

package agency.techartists.taanalytics.appsflyer

import agency.techartists.taanalytics.adaptor.AnalyticsAdaptor
import agency.techartists.taanalytics.adaptor.AnalyticsAdaptorWithReadOnlyUserPseudoID
import agency.techartists.taanalytics.adaptor.AnalyticsAdaptorWithWriteOnlyUserID
import agency.techartists.taanalytics.core.TAAnalyticsConfig
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.EventAnalyticsModelTrimmed
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModelTrimmed
import android.content.Context
import android.content.SharedPreferences
import com.appsflyer.AppsFlyerLib

/**
 * AppsFlyer Analytics adaptor for TAAnalytics.
 *
 * Implements user ID write-only and pseudo ID read capabilities.
 *
 * **Trimming:**
 * - Event names: 45 characters (AppsFlyer limit)
 * - User property names: No specific limit documented
 * - Parameter keys: 45 characters (AppsFlyer limit)
 * - String values: No explicit limit, but recommended to keep reasonable
 *
 * **Type conversion:**
 * - String parameters → String
 * - Int/Long parameters → Long
 * - Float/Double parameters → Double
 * - Boolean parameters → Boolean
 *
 * **Note:** AppsFlyer is primarily a mobile measurement partner (MMP) platform
 * designed for attribution and marketing analytics. Unlike general analytics platforms,
 * it focuses on install attribution, deep linking, and campaign tracking.
 *
 * Usage:
 * ```
 * val appsFlyerLib = AppsFlyerLib.getInstance()
 * val appsFlyerAdaptor = AppsFlyerAnalyticsAdaptor(context, appsFlyerLib)
 * val config = TAAnalyticsConfig(
 *     analyticsVersion = "1.0",
 *     adaptors = listOf(appsFlyerAdaptor),
 *     sharedPreferences = sharedPreferences
 * )
 * val analytics = TAAnalytics(context, config)
 * ```
 *
 * Before using, initialize AppsFlyer in your Application class:
 * ```
 * AppsFlyerLib.getInstance().init("YOUR_DEV_KEY", null, this)
 * AppsFlyerLib.getInstance().start(this)
 * ```
 */
class AppsFlyerAnalyticsAdaptor(
    private val context: Context,
    private val appsFlyerLib: AppsFlyerLib
) : AnalyticsAdaptor,
    AnalyticsAdaptorWithWriteOnlyUserID,
    AnalyticsAdaptorWithReadOnlyUserPseudoID {

    companion object {
        private const val EVENT_NAME_MAX_LENGTH = 45
        private const val USER_PROPERTY_NAME_MAX_LENGTH = 100
        private const val PARAMETER_KEY_MAX_LENGTH = 45
    }

    override suspend fun startFor(
        installType: TAAnalyticsConfig.InstallType,
        sharedPreferences: SharedPreferences
    ) {
        // AppsFlyer should be initialized in the Application class
        // This method is called after initialization
    }

    override fun track(
        trimmedEvent: EventAnalyticsModelTrimmed,
        params: Map<String, AnalyticsBaseParameterValue>?
    ) {
        val eventValues = mutableMapOf<String, Any>()

        params?.forEach { (key, value) ->
            val trimmedKey = key.take(PARAMETER_KEY_MAX_LENGTH)
            val actualValue = value.toAnalyticsValue()

            when (actualValue) {
                is String -> eventValues[trimmedKey] = actualValue
                is Int -> eventValues[trimmedKey] = actualValue
                is Long -> eventValues[trimmedKey] = actualValue
                is Float -> eventValues[trimmedKey] = actualValue
                is Double -> eventValues[trimmedKey] = actualValue
                is Boolean -> eventValues[trimmedKey] = actualValue
                else -> eventValues[trimmedKey] = actualValue.toString()
            }
        }

        appsFlyerLib.logEvent(context, trimmedEvent.rawValue, eventValues)
    }

    override fun set(
        trimmedUserProperty: UserPropertyAnalyticsModelTrimmed,
        value: String?
    ) {
        // AppsFlyer uses custom user data API
        if (value != null) {
            appsFlyerLib.setAdditionalData(mapOf(trimmedUserProperty.rawValue to value))
        }
        // Note: AppsFlyer doesn't support unsetting properties directly
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
            appsFlyerLib.setCustomerUserId(userID)
        } else {
            // AppsFlyer doesn't provide a way to clear the customer user ID
            // Setting empty string as a workaround
            appsFlyerLib.setCustomerUserId("")
        }
    }

    override fun getUserPseudoID(): String? {
        // Return AppsFlyer's unique device ID
        return appsFlyerLib.getAppsFlyerUID(context)
    }
}
