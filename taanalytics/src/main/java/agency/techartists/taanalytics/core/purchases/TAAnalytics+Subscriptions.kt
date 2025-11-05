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

package agency.techartists.taanalytics.core.purchases

import agency.techartists.taanalytics.constants.Events
import agency.techartists.taanalytics.core.EventLogCondition
import agency.techartists.taanalytics.core.TAAnalytics
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.toAnalyticsValue

/**
 * Track when a user starts a subscription with an intro offer.
 *
 * Sends a `subscription_start_intro` event with subscription details. Also automatically
 * sends a `subscription_start_new` event alongside it.
 *
 * Parameters sent:
 * - product_id: String
 * - type: String (subscription type, all except .paidRegular)
 * - placement: String
 * - paywall_id: String? (if provided)
 * - paywall_name: String? (if provided)
 * - value: Float (price)
 * - price: Float
 * - currency: String
 * - quantity: Int (always 1)
 *
 * @param sub The subscription analytics data
 */
fun TAAnalytics.trackSubscriptionStartIntro(sub: TASubscriptionStartAnalytics) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    addSubscriptionParameters(params, sub)

    track(
        event = Events.SUBSCRIPTION_START_INTRO,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )

    trackSubscriptionStartNew(sub)
}

/**
 * Track when a user starts a regular paid subscription (without intro offer).
 *
 * Sends a `subscription_start_paid_regular` event with subscription details. Also automatically
 * sends a `subscription_start_new` event alongside it.
 *
 * Parameters sent:
 * - product_id: String
 * - type: String (always .paidRegular)
 * - placement: String
 * - paywall_id: String? (if provided)
 * - paywall_name: String? (if provided)
 * - value: Float (price)
 * - price: Float
 * - currency: String
 * - quantity: Int (always 1)
 *
 * @param sub The subscription analytics data
 */
fun TAAnalytics.trackSubscriptionStartPaidRegular(sub: TASubscriptionStartAnalytics) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    addSubscriptionParameters(params, sub)

    track(
        event = Events.SUBSCRIPTION_START_PAID_REGULAR,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )

    trackSubscriptionStartNew(sub)
}

/**
 * Track when a user starts any new subscription.
 *
 * **DO NOT USE THIS DIRECTLY.** Instead, use [trackSubscriptionStartIntro] or
 * [trackSubscriptionStartPaidRegular], which automatically call this method.
 *
 * Sends a `subscription_start_new` event with subscription details.
 *
 * Parameters sent:
 * - product_id: String
 * - type: String (subscription type)
 * - placement: String
 * - paywall_id: String? (if provided)
 * - paywall_name: String? (if provided)
 * - value: Float (price)
 * - price: Float
 * - currency: String
 * - quantity: Int (always 1)
 *
 * @param sub The subscription analytics data
 */
fun TAAnalytics.trackSubscriptionStartNew(sub: TASubscriptionStartAnalytics) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    addSubscriptionParameters(params, sub)

    track(
        event = Events.SUBSCRIPTION_START_NEW,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Track when a user restores a previous subscription.
 *
 * Sends a `subscription_restore` event with subscription details.
 *
 * Parameters sent:
 * - product_id: String
 * - type: String (subscription type)
 * - placement: String
 * - paywall_id: String? (if provided)
 * - paywall_name: String? (if provided)
 * - value: Float (price)
 * - price: Float
 * - currency: String
 * - quantity: Int (always 1)
 *
 * @param sub The subscription analytics data
 */
fun TAAnalytics.trackSubscriptionRestore(sub: TASubscriptionStartAnalytics) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    addSubscriptionParameters(params, sub)

    track(
        event = Events.SUBSCRIPTION_RESTORE,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Internal helper to add common subscription parameters to the event.
 */
private fun addSubscriptionParameters(
    params: MutableMap<String, AnalyticsBaseParameterValue>,
    sub: TASubscriptionStartAnalytics
) {
    params["product_id"] = sub.productID.toAnalyticsValue()
    params["type"] = sub.subscriptionType.toString().toAnalyticsValue()
    params["placement"] = sub.paywall.analyticsPlacement.toAnalyticsValue()

    params["value"] = sub.price.toAnalyticsValue()
    params["price"] = sub.price.toAnalyticsValue()
    params["currency"] = sub.currency.toAnalyticsValue()
    params["quantity"] = 1.toAnalyticsValue()

    sub.paywall.analyticsID?.let { params["paywall_id"] = it.toAnalyticsValue() }
    sub.paywall.analyticsName?.let { params["paywall_name"] = it.toAnalyticsValue() }
}
