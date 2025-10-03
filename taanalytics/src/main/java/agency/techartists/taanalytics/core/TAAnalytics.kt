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

import agency.techartists.taanalytics.constants.Events
import agency.techartists.taanalytics.constants.UserProperties
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.toAnalyticsValue
import agency.techartists.taanalytics.utils.InstallUserPropertiesCalculator
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withTimeout

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
    private val eventBuffer = EventBuffer()
    private var lifecycleObserver: AppLifecycleObserver? = null

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
     * Whether this is the first open ever for this app.
     */
    val isFirstOpen: Boolean
        get() = config.sharedPreferences.getBoolean("$PREFS_KEY_PREFIX.isFirstOpen", true)

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

        // Start all adaptors in parallel with timeout
        val installType = TAAnalyticsConfig.findInstallType(context)
        val startedAdaptors = config.adaptors.map { adaptor ->
            scope.async {
                try {
                    withTimeout(config.maxTimeoutForAdaptorStart) {
                        adaptor.startFor(
                            installType = installType,
                            sharedPreferences = config.sharedPreferences
                        )
                    }
                    Log.i(TAG, "Adaptor ${adaptor::class.simpleName} started successfully")
                    adaptor
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start adaptor ${adaptor::class.simpleName}: ${e.message}", e)
                    null
                }
            }
        }.awaitAll().filterNotNull()

        // Setup event buffer with started adaptors
        eventBuffer.setupAdaptors(startedAdaptors)

        isStarted = true

        // Set analytics version as user property
        set(UserProperties.ANALYTICS_VERSION, config.analyticsVersion)

        // Increment cold launch count
        incrementColdLaunchCount()

        // Check for app version updates
        checkAndTrackAppVersionUpdate()

        // Check for OS version updates
        checkAndTrackOSVersionUpdate()

        // Handle first open
        if (isFirstOpen) {
            handleFirstOpen()
        }

        // Start lifecycle observer for automatic APP_OPEN/APP_CLOSE tracking
        lifecycleObserver = AppLifecycleObserver(this)
        lifecycleObserver?.startObserving()
    }

    /**
     * Increment the cold launch count user property.
     */
    private fun incrementColdLaunchCount() {
        val currentCount = get(UserProperties.APP_COLD_LAUNCH_COUNT)?.toIntOrNull() ?: 0
        val newCount = currentCount + 1
        set(UserProperties.APP_COLD_LAUNCH_COUNT, newCount.toString())
        Log.d(TAG, "Cold launch count: $newCount")
    }

    /**
     * Check if app version has changed and track update event.
     */
    private fun checkAndTrackAppVersionUpdate() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = packageInfo.versionName ?: "unknown"
            @Suppress("DEPRECATION")
            val currentBuild = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                packageInfo.versionCode.toString()
            }

            val savedVersion = config.sharedPreferences.getString("$PREFS_KEY_PREFIX.appVersion", null)
            val savedBuild = config.sharedPreferences.getString("$PREFS_KEY_PREFIX.appBuild", null)

            if (savedVersion != null && (savedVersion != currentVersion || savedBuild != currentBuild)) {
                // Version changed
                Log.i(TAG, "App version updated: $savedVersion ($savedBuild) -> $currentVersion ($currentBuild)")

                track(
                    event = Events.APP_VERSION_UPDATE,
                    params = mapOf(
                        "from_version" to (savedVersion ?: "unknown").toAnalyticsValue(),
                        "to_version" to currentVersion.toAnalyticsValue(),
                        "from_build" to (savedBuild ?: "unknown").toAnalyticsValue(),
                        "to_build" to currentBuild.toAnalyticsValue()
                    )
                )
            }

            // Save current version
            config.sharedPreferences.edit {
                putString("$PREFS_KEY_PREFIX.appVersion", currentVersion)
                putString("$PREFS_KEY_PREFIX.appBuild", currentBuild)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking app version: ${e.message}", e)
        }
    }

    /**
     * Check if OS version has changed and track update event.
     */
    private fun checkAndTrackOSVersionUpdate() {
        val currentOSVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        val savedOSVersion = config.sharedPreferences.getString("$PREFS_KEY_PREFIX.osVersion", null)

        if (savedOSVersion != null && savedOSVersion != currentOSVersion) {
            Log.i(TAG, "OS version updated: $savedOSVersion -> $currentOSVersion")

            track(
                event = Events.OS_VERSION_UPDATE,
                params = mapOf(
                    "from_version" to savedOSVersion.toAnalyticsValue(),
                    "to_version" to currentOSVersion.toAnalyticsValue()
                )
            )
        }

        // Save current OS version
        config.sharedPreferences.edit {
            putString("$PREFS_KEY_PREFIX.osVersion", currentOSVersion)
        }
    }

    /**
     * Handle first open: calculate install properties, track event, and mark as no longer first open.
     */
    private fun handleFirstOpen() {
        Log.i(TAG, "First open detected")

        // Calculate and set install-time user properties
        val calculator = InstallUserPropertiesCalculator(
            context = context,
            analytics = this,
            userPropertiesToCalculate = config.installUserProperties
        )
        calculator.calculateAndSetUserProperties()

        // Track first open event
        track(
            event = Events.OUR_FIRST_OPEN,
            params = null,
            logCondition = EventLogCondition.LOG_ONLY_ONCE_PER_LIFETIME
        )

        // Mark as no longer first open
        config.sharedPreferences.edit {
            putBoolean("$PREFS_KEY_PREFIX.isFirstOpen", false)
        }
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

        // Track using event buffer
        eventBuffer.addEvent(prefixedEvent, params)
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
        eventBuffer.getStartedAdaptors().forEach { adaptor ->
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
