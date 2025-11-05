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

/**
 * Track when the user enters the onboarding flow.
 *
 * Sends an `onboarding_enter` event with optional extra parameters.
 *
 * @param extraParams Optional additional parameters to include with the event
 */
fun TAAnalytics.trackOnboardingEnter(
    extraParams: Map<String, AnalyticsBaseParameterValue>? = null
) {
    track(
        event = Events.ONBOARDING_ENTER,
        params = extraParams,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Track when the user exits/completes the onboarding flow.
 *
 * Sends an `onboarding_exit` event with optional extra parameters.
 *
 * @param extraParams Optional additional parameters to include with the event
 */
fun TAAnalytics.trackOnboardingExit(
    extraParams: Map<String, AnalyticsBaseParameterValue>? = null
) {
    track(
        event = Events.ONBOARDING_EXIT,
        params = extraParams,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}
