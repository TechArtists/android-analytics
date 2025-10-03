//  StuckUIManager.kt
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

import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.toAnalyticsValue
import android.os.Handler
import android.os.Looper

/**
 * Manages stuck UI detection for transient views.
 *
 * Tracks when a view is shown for longer than expected (e.g., splash screen > 5 seconds).
 * Automatically sends `error reason=stuck on ui_view_show` when threshold exceeded.
 * Sends `error_corrected` if the view transitions within 30 seconds of the stuck event.
 */
class StuckUIManager(
    private val viewParams: Map<String, AnalyticsBaseParameterValue>,
    private val initialDelayMillis: Long,
    private val analytics: TAAnalytics
) {
    companion object {
        const val REASON = "stuck on ui_view_show"
        private const val CORRECTION_WINDOW_MILLIS = 30_000L // 30 seconds
    }

    private val handler = Handler(Looper.getMainLooper())
    private var stuckRunnable: Runnable? = null
    private var waitingForCorrectionSince: Long? = null

    init {
        scheduleStuckCheck()
    }

    private fun scheduleStuckCheck() {
        stuckRunnable = Runnable {
            handleStuckTimeout()
        }
        handler.postDelayed(stuckRunnable!!, initialDelayMillis)
    }

    private fun handleStuckTimeout() {
        val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

        // Add view_ prefix to all view parameters
        viewParams.forEach { (key, value) ->
            params["view_$key"] = value
        }

        // Add duration parameter (in seconds)
        params["duration"] = (initialDelayMillis / 1000.0).toAnalyticsValue()

        // Track the stuck error
        analytics.trackErrorEvent(
            reason = REASON,
            error = null,
            extraParams = params
        )

        // Mark that we're waiting for correction
        waitingForCorrectionSince = System.currentTimeMillis()
    }

    /**
     * Call this when the view transitions to track error correction if within the window.
     */
    fun trackCorrectedIfNeeded() {
        val correctionStart = waitingForCorrectionSince ?: return

        val elapsedSinceCorrectionStart = System.currentTimeMillis() - correctionStart

        // Only track correction if within 30 second window
        if (elapsedSinceCorrectionStart <= CORRECTION_WINDOW_MILLIS) {
            val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

            // Add view_ prefix to all view parameters
            viewParams.forEach { (key, value) ->
                params["view_$key"] = value
            }

            // Add total duration parameter (initial delay + time until correction)
            val totalDuration = (initialDelayMillis + elapsedSinceCorrectionStart) / 1000.0
            params["duration"] = totalDuration.toAnalyticsValue()

            // Track the correction
            analytics.trackErrorCorrected(
                reason = REASON,
                error = null,
                extraParams = params
            )
        }
    }

    /**
     * Cancel the stuck check and cleanup.
     */
    fun cancel() {
        stuckRunnable?.let { handler.removeCallbacks(it) }
        stuckRunnable = null
    }
}
