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

import agency.techartists.taanalytics.constants.Events
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.toAnalyticsValue

/**
 * Track a general engagement action in your app.
 *
 * Sends an `engagement` event with the engagement name and automatically includes
 * context from the last view shown.
 *
 * Parameters sent:
 * - name: String (the engagement action name)
 * - view_name: String? (from last view shown)
 * - view_type: String? (from last view shown)
 * - view_funnel_name: String? (from last view shown)
 * - view_funnel_step: Int? (from last view shown)
 * - view_funnel_step_is_optional: Boolean? (from last view shown)
 * - view_funnel_step_is_final: Boolean? (from last view shown)
 *
 * Example: In a fitness app, you might track "start workout", "log set", "complete workout".
 *
 * @param engagement The name of the engagement action
 */
fun TAAnalytics.trackEngagement(engagement: String) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    params["name"] = engagement.toAnalyticsValue()

    // Add last view context
    lastViewShow?.let { view ->
        addViewParameters(view, params, prefix = "view_")
    }

    track(
        event = Events.ENGAGEMENT,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Track a primary engagement action in your app.
 *
 * Sends both `engagement_primary` AND `engagement` events with the engagement name and
 * automatically includes context from the last view shown.
 *
 * Use this for engagements that you consider the primary success driver of your app.
 * For example, in a fitness app, "complete workout" might be primary engagement while
 * "start workout" is regular engagement.
 *
 * Parameters sent:
 * - name: String (the engagement action name)
 * - view_name: String? (from last view shown)
 * - view_type: String? (from last view shown)
 * - view_funnel_name: String? (from last view shown)
 * - view_funnel_step: Int? (from last view shown)
 * - view_funnel_step_is_optional: Boolean? (from last view shown)
 * - view_funnel_step_is_final: Boolean? (from last view shown)
 *
 * @param engagement The name of the primary engagement action
 */
fun TAAnalytics.trackEngagementPrimary(engagement: String) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    params["name"] = engagement.toAnalyticsValue()

    // Add last view context
    lastViewShow?.let { view ->
        addViewParameters(view, params, prefix = "view_")
    }

    track(
        event = Events.ENGAGEMENT,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )

    track(
        event = Events.ENGAGEMENT_PRIMARY,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}
