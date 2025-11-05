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
import agency.techartists.taanalytics.core.track
import agency.techartists.taanalytics.core.trackButtonTap
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.ViewAnalyticsModel
import agency.techartists.taanalytics.models.toAnalyticsValue

/**
 * Track when a paywall is shown to the user.
 *
 * Sends a `paywall_show` event with paywall details and also sends a `ui_view_show` event
 * with `name="paywall"` and `type=<placement>`.
 *
 * Parameters sent:
 * - placement: String
 * - id: String? (if provided)
 * - name: String? (if provided)
 *
 * @param paywall The paywall being shown
 */
fun TAAnalytics.trackPaywallEnter(paywall: TAPaywallAnalytics) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

    params["placement"] = paywall.analyticsPlacement.toAnalyticsValue()
    paywall.analyticsID?.let { params["id"] = it.toAnalyticsValue() }
    paywall.analyticsName?.let { params["name"] = it.toAnalyticsValue() }

    track(
        event = Events.PAYWALL_ENTER,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )

    track(ViewAnalyticsModel(name = "paywall", type = paywall.analyticsPlacement))
}

/**
 * Track when a user exits a paywall.
 *
 * Sends a `paywall_exit` event with paywall details and the exit reason.
 *
 * Parameters sent:
 * - placement: String
 * - id: String? (if provided)
 * - name: String? (if provided)
 * - reason: String
 *
 * @param paywall The paywall being exited
 * @param reason The reason for exiting the paywall
 */
fun TAAnalytics.trackPaywallExit(paywall: TAPaywallAnalytics, reason: TAPaywallExitReason) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

    params["placement"] = paywall.analyticsPlacement.toAnalyticsValue()
    params["reason"] = reason.toString().toAnalyticsValue()
    paywall.analyticsID?.let { params["id"] = it.toAnalyticsValue() }
    paywall.analyticsName?.let { params["name"] = it.toAnalyticsValue() }

    track(
        event = Events.PAYWALL_EXIT,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Track when a user taps a purchase button on a paywall.
 *
 * Sends a `paywall_purchase_tap` event with button and product details. Also sends a
 * `ui_button_tap` event with `name=<buttonName>`, `view_name="paywall"`, `view_type=<placement>`.
 *
 * Parameters sent:
 * - button_name: String
 * - product_id: String
 * - placement: String
 * - paywall_id: String? (if provided)
 * - paywall_name: String? (if provided)
 *
 * @param buttonName The symbolic name of the button that was pressed (usually the English variant,
 *                   e.g., "Try free before subscribing" vs "Subscribe Now")
 * @param productIdentifier The product identifier from the Play Store
 * @param paywall The paywall where the button was tapped
 */
fun TAAnalytics.trackPaywallPurchaseTap(
    buttonName: String,
    productIdentifier: String,
    paywall: TAPaywallAnalytics
) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

    params["button_name"] = buttonName.toAnalyticsValue()
    params["product_id"] = productIdentifier.toAnalyticsValue()
    params["placement"] = paywall.analyticsPlacement.toAnalyticsValue()
    paywall.analyticsID?.let { params["paywall_id"] = it.toAnalyticsValue() }
    paywall.analyticsName?.let { params["paywall_name"] = it.toAnalyticsValue() }

    track(
        event = Events.PAYWALL_PURCHASE_TAP,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )

    trackButtonTap(
        symbolicName = buttonName,
        onView = ViewAnalyticsModel(name = "paywall", type = paywall.analyticsPlacement)
    )
}
