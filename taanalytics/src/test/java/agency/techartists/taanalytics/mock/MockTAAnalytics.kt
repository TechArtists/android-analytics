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

import agency.techartists.taanalytics.core.EventLogCondition
import agency.techartists.taanalytics.core.StuckUIManager
import agency.techartists.taanalytics.core.TAPermissionType
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.IViewAnalyticsModel
import agency.techartists.taanalytics.models.SecondaryViewAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.ViewAnalyticsModel
import java.util.UUID

/**
 * Mock implementation of TAAnalytics for unit testing.
 *
 * Records all tracked events and user properties for verification in tests.
 * Does not perform any actual analytics tracking or network calls.
 *
 * Usage:
 * ```kotlin
 * val mockAnalytics = MockTAAnalytics()
 * val feature = MyFeature(mockAnalytics)
 * feature.doSomething()
 *
 * // Assert
 * assertEquals(1, mockAnalytics.eventsSent.size)
 * assertEquals("button_clicked", mockAnalytics.eventsSent[0].first.rawValue)
 * ```
 */
class MockTAAnalytics {

    /**
     * All events tracked during this mock's lifetime.
     * List of (event, parameters) pairs.
     */
    val eventsSent = mutableListOf<Pair<EventAnalyticsModel, Map<String, AnalyticsBaseParameterValue>?>>()

    /**
     * All user properties set during this mock's lifetime.
     */
    val userPropertiesSet = mutableMapOf<UserPropertyAnalyticsModel, String>()

    /**
     * Last view that was tracked.
     */
    var lastViewShow: ViewAnalyticsModel? = null

    /**
     * Current stuck UI manager (for testing stuck UI detection).
     */
    var stuckUIManager: StuckUIManager? = null

    /**
     * Mock user ID.
     */
    var userID: String? = null

    /**
     * Mock user pseudo ID (platform-generated).
     */
    var userPseudoID: String? = UUID.randomUUID().toString()

    /**
     * Track an event.
     */
    fun track(
        event: EventAnalyticsModel,
        params: Map<String, AnalyticsBaseParameterValue>? = null,
        logCondition: EventLogCondition = EventLogCondition.LOG_ALWAYS
    ) {
        eventsSent.add(event to params)
    }

    /**
     * Set a user property.
     */
    fun set(userProperty: UserPropertyAnalyticsModel, value: String?) {
        if (value != null) {
            userPropertiesSet[userProperty] = value
        } else {
            userPropertiesSet.remove(userProperty)
        }
    }

    /**
     * Get a user property.
     */
    fun get(userProperty: UserPropertyAnalyticsModel): String? {
        return userPropertiesSet[userProperty]
    }

    /**
     * Track a view show.
     */
    fun track(view: ViewAnalyticsModel, stuckTimeout: Long? = null) {
        lastViewShow = view
        val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
        // Simplified - just record the event
        track(EventAnalyticsModel("ui_view_show"), params)
    }

    /**
     * Track a secondary view show.
     */
    fun track(secondaryView: SecondaryViewAnalyticsModel) {
        val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
        track(EventAnalyticsModel("ui_view_show"), params)
    }

    /**
     * Track a button tap.
     */
    fun trackButtonTap(
        symbolicName: String,
        onView: IViewAnalyticsModel,
        extra: String? = null,
        index: Int? = null
    ) {
        val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
        track(EventAnalyticsModel("ui_button_tap"), params)
    }

    /**
     * Track an error event.
     */
    fun trackErrorEvent(
        reason: String,
        error: Throwable? = null,
        extraParams: Map<String, AnalyticsBaseParameterValue>? = null
    ) {
        val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
        track(EventAnalyticsModel("error"), params)
    }

    /**
     * Track an error corrected event.
     */
    fun trackErrorCorrected(
        reason: String,
        error: Throwable? = null,
        extraParams: Map<String, AnalyticsBaseParameterValue>? = null
    ) {
        val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
        track(EventAnalyticsModel("error_corrected"), params)
    }

    /**
     * Track permission screen show.
     */
    fun trackPermissionScreenShow(permissionType: TAPermissionType) {
        val view = ViewAnalyticsModel("permission", type = permissionType.toString())
        track(view)
    }

    /**
     * Track permission screen show (custom type).
     */
    fun trackPermissionScreenShow(customPermissionType: String) {
        val view = ViewAnalyticsModel("permission", type = customPermissionType)
        track(view)
    }

    /**
     * Track permission button tap.
     */
    fun trackPermissionButtonTap(allowed: Boolean, permissionType: TAPermissionType) {
        val view = ViewAnalyticsModel("permission", type = permissionType.toString())
        trackButtonTap(if (allowed) "allow" else "dont_allow", view)
    }

    /**
     * Track permission button tap with status.
     */
    fun trackPermissionButtonTap(status: String, permissionType: TAPermissionType) {
        val view = ViewAnalyticsModel("permission", type = permissionType.toString())
        trackButtonTap(status, view)
    }

    /**
     * Clear all recorded data.
     */
    fun clear() {
        eventsSent.clear()
        userPropertiesSet.clear()
        lastViewShow = null
        stuckUIManager = null
        userID = null
    }

    /**
     * Find events by name.
     */
    fun findEvents(eventName: String): List<Pair<EventAnalyticsModel, Map<String, AnalyticsBaseParameterValue>?>> {
        return eventsSent.filter { it.first.rawValue == eventName }
    }

    /**
     * Find the last event with the given name.
     */
    fun findLastEvent(eventName: String): Pair<EventAnalyticsModel, Map<String, AnalyticsBaseParameterValue>?>? {
        return eventsSent.lastOrNull { it.first.rawValue == eventName }
    }
}
