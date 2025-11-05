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
 * Represents the signup method type.
 */
sealed class TASignupMethodType {
    /** Email-based signup */
    object Email : TASignupMethodType()

    /** Apple Sign In */
    object Apple : TASignupMethodType()

    /** Google Sign In */
    object Google : TASignupMethodType()

    /** Facebook Sign In */
    object Facebook : TASignupMethodType()

    /** Custom signup method */
    data class Custom(val method: String) : TASignupMethodType()

    override fun toString(): String = when (this) {
        is Email -> "email"
        is Apple -> "apple"
        is Google -> "google"
        is Facebook -> "facebook"
        is Custom -> method
    }
}

/**
 * Track when the user enters the account signup flow.
 *
 * Sends an `account_signup_enter` event with optional signup method and extra parameters.
 *
 * Parameters sent:
 * - method: String? (if provided)
 * - Additional custom parameters from extraParams
 *
 * @param method The signup method (optional, can be specified at enter or exit)
 * @param extraParams Optional additional parameters to include with the event
 */
fun TAAnalytics.trackAccountSignupEnter(
    method: TASignupMethodType? = null,
    extraParams: Map<String, AnalyticsBaseParameterValue>? = null
) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

    method?.let { params["method"] = it.toString().toAnalyticsValue() }
    extraParams?.forEach { (key, value) -> params[key] = value }

    track(
        event = Events.ACCOUNT_SIGNUP_ENTER,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Track when the user exits/completes the account signup flow.
 *
 * Sends an `account_signup_exit` event with signup method and optional extra parameters.
 *
 * Parameters sent:
 * - method: String (required at exit)
 * - Additional custom parameters from extraParams
 *
 * @param method The signup method used
 * @param extraParams Optional additional parameters to include with the event
 */
fun TAAnalytics.trackAccountSignupExit(
    method: TASignupMethodType,
    extraParams: Map<String, AnalyticsBaseParameterValue>? = null
) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

    params["method"] = method.toString().toAnalyticsValue()
    extraParams?.forEach { (key, value) -> params[key] = value }

    track(
        event = Events.ACCOUNT_SIGNUP_EXIT,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}
