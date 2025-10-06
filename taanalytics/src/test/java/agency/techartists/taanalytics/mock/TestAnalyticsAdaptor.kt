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

package agency.techartists.taanalytics.mock

import agency.techartists.taanalytics.adaptor.AnalyticsAdaptor
import agency.techartists.taanalytics.core.TAAnalyticsConfig
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.EventAnalyticsModelTrimmed
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModelTrimmed
import android.content.SharedPreferences

/**
 * Test-only analytics adaptor for unit testing.
 *
 * Records all tracked events and user properties in their trimmed form,
 * allowing verification of the entire event flow including trimming logic.
 *
 * Usage:
 * ```kotlin
 * val testAdaptor = TestAnalyticsAdaptor()
 * val analytics = TAAnalytics(context, TAAnalyticsConfig(
 *     analyticsVersion = "1.0",
 *     adaptors = listOf(testAdaptor),
 *     sharedPreferences = mockSharedPreferences
 * ))
 * analytics.start()
 *
 * // ... perform actions
 *
 * assertEquals(1, testAdaptor.eventsSent.size)
 * assertEquals("ui_button_tap", testAdaptor.eventsSent[0].first.rawValue)
 * ```
 */
class TestAnalyticsAdaptor(
    private val eventTrimLength: Int = 40,
    private val userPropertyTrimLength: Int = 24
) : AnalyticsAdaptor {

    /**
     * All events sent to this adaptor (trimmed).
     */
    val eventsSent = mutableListOf<Pair<EventAnalyticsModelTrimmed, Map<String, AnalyticsBaseParameterValue>?>>()

    /**
     * All user properties set on this adaptor (trimmed).
     */
    val userPropertiesSet = mutableMapOf<UserPropertyAnalyticsModelTrimmed, String>()

    /**
     * Whether this adaptor has been started.
     */
    var isStarted = false
        private set

    /**
     * Install type used during startup.
     */
    var installType: TAAnalyticsConfig.InstallType? = null
        private set

    override suspend fun startFor(
        installType: TAAnalyticsConfig.InstallType,
        sharedPreferences: SharedPreferences
    ) {
        this.installType = installType
        isStarted = true
    }

    override fun track(
        trimmedEvent: EventAnalyticsModelTrimmed,
        params: Map<String, AnalyticsBaseParameterValue>?
    ) {
        eventsSent.add(trimmedEvent to params)
    }

    override fun set(trimmedUserProperty: UserPropertyAnalyticsModelTrimmed, value: String?) {
        if (value != null) {
            userPropertiesSet[trimmedUserProperty] = value
        } else {
            userPropertiesSet.remove(trimmedUserProperty)
        }
    }

    override fun trim(event: EventAnalyticsModel): EventAnalyticsModelTrimmed {
        return EventAnalyticsModelTrimmed(event.rawValue.take(eventTrimLength))
    }

    override fun trim(userProperty: UserPropertyAnalyticsModel): UserPropertyAnalyticsModelTrimmed {
        return UserPropertyAnalyticsModelTrimmed(userProperty.rawValue.take(userPropertyTrimLength))
    }

    /**
     * Clear all recorded data.
     */
    fun clear() {
        eventsSent.clear()
        userPropertiesSet.clear()
    }

    /**
     * Find events by name.
     */
    fun findEvents(eventName: String): List<Pair<EventAnalyticsModelTrimmed, Map<String, AnalyticsBaseParameterValue>?>> {
        return eventsSent.filter { it.first.rawValue == eventName }
    }

    /**
     * Find the last event with the given name.
     */
    fun findLastEvent(eventName: String): Pair<EventAnalyticsModelTrimmed, Map<String, AnalyticsBaseParameterValue>?>? {
        return eventsSent.lastOrNull { it.first.rawValue == eventName }
    }

    /**
     * Assert that an event was sent.
     */
    fun assertEventSent(eventName: String, expectedParamCount: Int? = null) {
        val event = findLastEvent(eventName)
            ?: throw AssertionError("Event '$eventName' was not sent")

        if (expectedParamCount != null) {
            val actualCount = event.second?.size ?: 0
            if (actualCount != expectedParamCount) {
                throw AssertionError(
                    "Event '$eventName' has $actualCount parameters, expected $expectedParamCount"
                )
            }
        }
    }

    /**
     * Assert that a user property was set.
     */
    fun assertUserPropertySet(propertyName: String, expectedValue: String) {
        val property = UserPropertyAnalyticsModelTrimmed(propertyName.take(userPropertyTrimLength))
        val actualValue = userPropertiesSet[property]
            ?: throw AssertionError("User property '$propertyName' was not set")

        if (actualValue != expectedValue) {
            throw AssertionError(
                "User property '$propertyName' has value '$actualValue', expected '$expectedValue'"
            )
        }
    }
}
