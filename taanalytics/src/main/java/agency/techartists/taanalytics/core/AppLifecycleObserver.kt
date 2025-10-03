//  AppLifecycleObserver.kt
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
import agency.techartists.taanalytics.models.toAnalyticsValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import android.util.Log

/**
 * Observes app lifecycle and automatically tracks APP_OPEN and APP_CLOSE events.
 *
 * This uses ProcessLifecycleOwner to detect when the app goes to foreground/background.
 */
internal class AppLifecycleObserver(
    private val analytics: TAAnalytics
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "TAAnalytics.Lifecycle"
    }

    private var isColdLaunch = true

    /**
     * Start observing app lifecycle.
     */
    fun startObserving() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d(TAG, "Started observing app lifecycle")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // App went to foreground
        trackAppOpen()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // App went to background
        trackAppClose()
    }

    private fun trackAppOpen() {
        Log.d(TAG, "App opened (is_cold_launch=$isColdLaunch)")

        // Increment open count
        val currentCount = analytics.get(UserProperties.APP_OPEN_COUNT)?.toIntOrNull() ?: 0
        val newCount = currentCount + 1
        analytics.set(UserProperties.APP_OPEN_COUNT, newCount.toString())

        // Track app open event
        analytics.track(
            event = Events.APP_OPEN,
            params = mapOf(
                "is_cold_launch" to isColdLaunch.toAnalyticsValue(),
                "app_open_count" to newCount.toAnalyticsValue()
            ),
            logCondition = EventLogCondition.LOG_ALWAYS
        )

        // After first open, it's no longer a cold launch
        if (isColdLaunch) {
            isColdLaunch = false
        }
    }

    private fun trackAppClose() {
        Log.d(TAG, "App closed")

        analytics.track(
            event = Events.APP_CLOSE,
            params = null,
            logCondition = EventLogCondition.LOG_ALWAYS
        )
    }
}
