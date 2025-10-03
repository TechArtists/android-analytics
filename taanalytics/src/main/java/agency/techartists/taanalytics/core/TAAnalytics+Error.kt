//  TAAnalytics+Error.kt
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
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.toAnalyticsValue

/**
 * Track an error event with optional exception details.
 *
 * Sends an `error` event with:
 * - reason: String (mandatory)
 * - error_message: String? (if Throwable provided)
 * - error_class: String? (if Throwable provided)
 * - error_stacktrace: String? (if Throwable provided, first 1000 chars)
 * - any extra params provided
 *
 * @param reason Developer-friendly reason for the error (e.g., "could not find valid JWT token")
 * @param error Optional throwable/exception that triggered this error
 * @param extraParams Optional additional parameters (e.g., "user_id", "endpoint")
 */
fun TAAnalytics.trackErrorEvent(
    reason: String,
    error: Throwable? = null,
    extraParams: Map<String, AnalyticsBaseParameterValue>? = null
) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    params["reason"] = reason.toAnalyticsValue()

    if (error != null) {
        params["error_message"] = error.message?.toAnalyticsValue() ?: "".toAnalyticsValue()
        params["error_class"] = error.javaClass.simpleName.toAnalyticsValue()

        // Get stack trace as string, limit to 1000 chars
        val stackTrace = error.stackTraceToString().take(1000)
        params["error_stacktrace"] = stackTrace.toAnalyticsValue()
    }

    extraParams?.forEach { (key, value) ->
        params[key] = value
    }

    track(
        event = Events.ERROR,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Track an error corrected event.
 *
 * Use this to indicate that a previously tracked error state has been resolved.
 * Useful for measuring false positives in error tracking.
 *
 * Sends an `error_corrected` event with:
 * - reason: String (mandatory, should match the original error reason)
 * - error_message: String? (if Throwable provided)
 * - error_class: String? (if Throwable provided)
 * - error_stacktrace: String? (if Throwable provided, first 1000 chars)
 * - any extra params provided
 *
 * @param reason Developer-friendly reason that was previously tracked as an error
 * @param error Optional throwable/exception from the original error
 * @param extraParams Optional additional parameters
 */
fun TAAnalytics.trackErrorCorrected(
    reason: String,
    error: Throwable? = null,
    extraParams: Map<String, AnalyticsBaseParameterValue>? = null
) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    params["reason"] = reason.toAnalyticsValue()

    if (error != null) {
        params["error_message"] = error.message?.toAnalyticsValue() ?: "".toAnalyticsValue()
        params["error_class"] = error.javaClass.simpleName.toAnalyticsValue()

        // Get stack trace as string, limit to 1000 chars
        val stackTrace = error.stackTraceToString().take(1000)
        params["error_stacktrace"] = stackTrace.toAnalyticsValue()
    }

    extraParams?.forEach { (key, value) ->
        params[key] = value
    }

    track(
        event = Events.ERROR_CORRECTED,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}
