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

package agency.techartists.taanalytics.core

import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.toAnalyticsValue

/**
 * Track a debug event for temporary production debugging.
 *
 * Sends a `debug` event with a reason and optional extra parameters.
 *
 * Use this to temporarily track specific conditions or states in production that you
 * need to debug. The `debug` event name helps your data analytics team identify these
 * events and filter them appropriately.
 *
 * Parameters sent:
 * - reason: String (required) - A developer reason about what triggered the debug state
 * - Additional custom parameters via extraParams
 *
 * Example:
 * ```kotlin
 * analytics.trackDebugEvent(
 *     reason = "couldn't find any valid JWT token",
 *     extraParams = mapOf(
 *         "token_count" to 0.toAnalyticsValue(),
 *         "user_id" to userId.toAnalyticsValue()
 *     )
 * )
 * ```
 *
 * @param reason A developer-friendly reason describing why this debug event was triggered
 * @param extraParams Optional additional parameters to include with the debug event
 */
fun TAAnalytics.trackDebugEvent(
    reason: String,
    extraParams: Map<String, AnalyticsBaseParameterValue>? = null
) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    params["reason"] = reason.toAnalyticsValue()

    extraParams?.forEach { (key, value) -> params[key] = value }

    track(
        event = EventAnalyticsModel("debug"),
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}
