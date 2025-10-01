//  TAAnalytics.kt
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
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.core.content.edit

/**
 * Main TAAnalytics class that coordinates all analytics tracking operations.
 *
 * Initialize with a [TAAnalyticsConfig] and call [start] to begin tracking.
 */
class TAAnalytics(
    private val context: Context,
    private val config: TAAnalyticsConfig,
) {

    companion object {
        private const val TAG = "TAAnalytics"
        private const val PREFS_KEY_PREFIX = "TAAnalytics"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val startedAdaptors = mutableListOf<AnalyticsAdaptor>()

    /**
     * Events sent during this session that had the specific log condition of
     * [EventLogCondition.LOG_ONLY_ONCE_PER_APP_SESSION]
     */
    private val appSessionEvents = mutableSetOf<EventAnalyticsModel>()

    /**
     * Whether the adaptors have been initialized.
     */
    private var isStarted = false

    /**
     * Initialize the analytics system and start all configured adaptors.
     *
     * This should be called once during app initialization, typically in Application.onCreate().
     */
    suspend fun start() {
        if (isStarted) {
            Log.w(TAG, "TAAnalytics.start() called multiple times")
            return
        }

        Log.i(TAG, "Starting TAAnalytics with ${config.adaptors.size} adaptor(s)")

        // Start all adaptors
        val installType = TAAnalyticsConfig.findInstallType(context)
        config.adaptors.forEach { adaptor ->
            try {
                adaptor.startFor(
                    installType = installType,
                    sharedPreferences = config.sharedPreferences
                )
                startedAdaptors.add(adaptor)
                Log.i(TAG, "Adaptor ${adaptor::class.simpleName} started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start adaptor ${adaptor::class.simpleName}: ${e.message}", e)
            }
        }

        isStarted = true

        // Set analytics version as user property
        set(UserPropertyAnalyticsModel("analytics_version", isInternalUserProperty = true), config.analyticsVersion)
    }

    /**
     * Track an analytics event.
     *
     * @param event The event to track
     * @param params Optional parameters for the event
     * @param logCondition Condition determining how often this event should be logged
     */
    fun track(
        event: EventAnalyticsModel,
        params: Map<String, AnalyticsBaseParameterValue>? = null,
        logCondition: EventLogCondition = EventLogCondition.LOG_ALWAYS,
    ) {
        if (!isStarted) {
            Log.w(TAG, "Attempted to track event before start() was called")
            return
        }

        val prefixedEvent = prefixEventIfNeeded(event)

        // Check log condition
        val shouldLog = when (logCondition) {
            EventLogCondition.LOG_ALWAYS -> true

            EventLogCondition.LOG_ONLY_ONCE_PER_LIFETIME -> {
                val key = "$PREFS_KEY_PREFIX.onlyOnce.${prefixedEvent.rawValue}"
                val hasBeenLogged = config.sharedPreferences.getBoolean(key, false)
                if (!hasBeenLogged) {
                    config.sharedPreferences.edit { putBoolean(key, true) }
                    true
                } else {
                    false
                }
            }

            EventLogCondition.LOG_ONLY_ONCE_PER_APP_SESSION -> {
                if (!appSessionEvents.contains(event)) {
                    appSessionEvents.add(event)
                    true
                } else {
                    false
                }
            }
        }

        if (!shouldLog) {
            return
        }

        // Apply event filter
        if (!config.trackEventFilter(event, params)) {
            return
        }

        // Track in all started adaptors
        scope.launch {
            startedAdaptors.forEach { adaptor ->
                try {
                    adaptor.track(adaptor.trim(prefixedEvent), params)
                } catch (e: Exception) {
                    Log.e(TAG, "Error tracking event in ${adaptor::class.simpleName}: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Set a user property.
     *
     * @param userProperty The user property to set
     * @param value The value to set (null to unset)
     */
    fun set(userProperty: UserPropertyAnalyticsModel, value: String?) {
        if (!isStarted) {
            Log.w(TAG, "Attempted to set user property before start() was called")
            return
        }

        val prefixedUserProperty = prefixUserPropertyIfNeeded(userProperty)

        // Save to SharedPreferences
        val key = "$PREFS_KEY_PREFIX.userProperty.${prefixedUserProperty.rawValue}"
        if (value != null) {
            config.sharedPreferences.edit { putString(key, value) }
        } else {
            config.sharedPreferences.edit { remove(key) }
        }

        // Set in all started adaptors
        startedAdaptors.forEach { adaptor ->
            try {
                adaptor.set(adaptor.trim(prefixedUserProperty), value)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting user property in ${adaptor::class.simpleName}: ${e.message}", e)
            }
        }
    }

    /**
     * Get a user property value from local storage.
     *
     * @param userProperty The user property to get
     * @return The stored value, or null if not set
     */
    fun get(userProperty: UserPropertyAnalyticsModel): String? {
        val prefixedUserProperty = prefixUserPropertyIfNeeded(userProperty)
        val key = "$PREFS_KEY_PREFIX.userProperty.${prefixedUserProperty.rawValue}"
        return config.sharedPreferences.getString(key, null)
    }

    /**
     * Apply prefix to event name if needed, based on whether it's internal or manual.
     */
    private fun prefixEventIfNeeded(event: EventAnalyticsModel): EventAnalyticsModel {
        return if (event.isInternalEvent) {
            event.eventByPrefixing(config.automaticallyTrackedEventsPrefixConfig.eventPrefix)
        } else {
            event.eventByPrefixing(config.manuallyTrackedEventsPrefixConfig.eventPrefix)
        }
    }

    /**
     * Apply prefix to user property name if needed, based on whether it's internal or manual.
     */
    private fun prefixUserPropertyIfNeeded(userProperty: UserPropertyAnalyticsModel): UserPropertyAnalyticsModel {
        return if (userProperty.isInternalUserProperty) {
            userProperty.userPropertyByPrefixing(config.automaticallyTrackedEventsPrefixConfig.userPropertyPrefix)
        } else {
            userProperty.userPropertyByPrefixing(config.manuallyTrackedEventsPrefixConfig.userPropertyPrefix)
        }
    }
}
