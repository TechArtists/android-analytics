//  AnalyticsAdaptor.kt
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

package agency.techartists.taanalytics.adaptor

import agency.techartists.taanalytics.core.TAAnalyticsConfig
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.EventAnalyticsModelTrimmed
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModelTrimmed
import android.content.SharedPreferences

/**
 * Protocol that defines methods for starting an analytics adaptor, logging events,
 * and setting user properties.
 *
 * Classes that implement this interface will handle these operations for different
 * analytics platforms (Firebase, Amplitude, etc.).
 */
interface AnalyticsAdaptor {

    /**
     * Starts the adaptor if it can for the required install type.
     *
     * @param installType The type of installation
     * @param sharedPreferences SharedPreferences to use for persistence
     * @throws Exception if the adaptor cannot start for this install type
     */
    suspend fun startFor(
        installType: TAAnalyticsConfig.InstallType,
        sharedPreferences: SharedPreferences
    )

    /**
     * Tracks an event with optional parameters.
     * This enforces trimming before calling the adaptor-specific implementation.
     *
     * @param trimmedEvent The event to track (already trimmed to platform limits)
     * @param params Optional parameters for the event
     */
    fun track(
        trimmedEvent: EventAnalyticsModelTrimmed,
        params: Map<String, AnalyticsBaseParameterValue>?
    )

    /**
     * Sets a user property value.
     *
     * @param trimmedUserProperty The user property to set (already trimmed to platform limits)
     * @param value The value to set (null to unset)
     */
    fun set(
        trimmedUserProperty: UserPropertyAnalyticsModelTrimmed,
        value: String?
    )

    /**
     * Adaptors should implement this to define how they trim the event name
     * to match platform-specific limits.
     */
    fun trim(event: EventAnalyticsModel): EventAnalyticsModelTrimmed

    /**
     * Adaptors should implement this to define how they trim the user property name
     * to match platform-specific limits.
     */
    fun trim(userProperty: UserPropertyAnalyticsModel): UserPropertyAnalyticsModelTrimmed
}

/**
 * Optional protocol for adaptors that provide a read-only user pseudo ID.
 *
 * This is typically an automatically generated ID by the analytics platform
 * (e.g., Firebase App Instance ID).
 */
interface AnalyticsAdaptorWithReadOnlyUserPseudoID {
    /**
     * Get the platform-generated pseudo ID for the current user.
     *
     * @return The pseudo ID, or null if not available
     */
    fun getUserPseudoID(): String?
}

/**
 * Optional protocol for adaptors that support setting a user ID (write-only).
 *
 * This is used for platforms like Crashlytics where you can set a user ID
 * but cannot read it back.
 */
interface AnalyticsAdaptorWithWriteOnlyUserID {
    /**
     * Set the user ID for this analytics session.
     *
     * @param userID The user ID to set, or null to clear
     */
    fun setUserID(userID: String?)
}

/**
 * Optional protocol for adaptors that support both reading and writing a user ID.
 *
 * This is used for platforms where you can both set and retrieve the user ID.
 */
interface AnalyticsAdaptorWithReadWriteUserID {
    /**
     * Set the user ID for this analytics session.
     *
     * @param userID The user ID to set, or null to clear
     */
    fun setUserID(userID: String?)

    /**
     * Get the current user ID.
     *
     * @return The user ID, or null if not set
     */
    fun getUserID(): String?
}
