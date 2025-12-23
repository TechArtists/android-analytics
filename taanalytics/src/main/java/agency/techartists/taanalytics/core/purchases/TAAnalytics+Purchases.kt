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
 * Track when a user completes a non-consumable one-time purchase.
 *
 * Sends a `purchase_non_consumable_one_time` event with purchase details. Also automatically
 * sends a `purchase_new` event alongside it.
 *
 * Parameters sent:
 * - product_id: String
 * - type: String (purchase type)
 * - placement: String
 * - paywall_id: String? (if provided)
 * - paywall_name: String? (if provided)
 * - value: Float (price)
 * - price: Float
 * - currency: String
 * - quantity: Int (always 1)
 *
 * @param purchase The purchase analytics data
 */
fun TAAnalytics.trackPurchaseNonConsumableOneTime(purchase: TAPurchaseAnalytics) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    addPurchaseParameters(params, purchase)

    track(
        event = Events.PURCHASE_NON_CONSUMABLE_ONE_TIME,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )

    trackPurchaseNew(purchase)
}

/**
 * Track when a user completes a consumable purchase.
 *
 * Sends a `purchase_consumable` event with purchase details. Also automatically
 * sends a `purchase_new` event alongside it.
 *
 * Parameters sent:
 * - product_id: String
 * - type: String (purchase type)
 * - placement: String
 * - paywall_id: String? (if provided)
 * - paywall_name: String? (if provided)
 * - value: Float (price)
 * - price: Float
 * - currency: String
 * - quantity: Int (always 1)
 *
 * @param purchase The purchase analytics data
 */
fun TAAnalytics.trackPurchaseConsumable(purchase: TAPurchaseAnalytics) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    addPurchaseParameters(params, purchase)

    track(
        event = Events.PURCHASE_CONSUMABLE,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )

    trackPurchaseNew(purchase)
}

/**
 * Track when a user completes any new purchase.
 *
 * **DO NOT USE THIS DIRECTLY.** Instead, use [trackPurchaseNonConsumableOneTime] or
 * [trackPurchaseConsumable], which automatically call this method.
 *
 * Sends a `purchase_new` event with purchase details.
 *
 * Parameters sent:
 * - product_id: String
 * - type: String (purchase type)
 * - placement: String
 * - paywall_id: String? (if provided)
 * - paywall_name: String? (if provided)
 * - value: Float (price)
 * - price: Float
 * - currency: String
 * - quantity: Int (always 1)
 *
 * @param purchase The purchase analytics data
 */
fun TAAnalytics.trackPurchaseNew(purchase: TAPurchaseAnalytics) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()
    addPurchaseParameters(params, purchase)

    track(
        event = Events.PURCHASE_NEW,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Internal helper to add common purchase parameters to the event.
 */
private fun addPurchaseParameters(
    params: MutableMap<String, AnalyticsBaseParameterValue>,
    purchase: TAPurchaseAnalytics
) {
    params["product_id"] = purchase.productID.toAnalyticsValue()
    params["type"] = purchase.purchaseType.toString().toAnalyticsValue()
    params["placement"] = purchase.paywall.analyticsPlacement.toAnalyticsValue()

    params["value"] = purchase.price.toAnalyticsValue()
    params["price"] = purchase.price.toAnalyticsValue()
    params["currency"] = purchase.currency.toAnalyticsValue()
    params["quantity"] = 1.toAnalyticsValue()

    purchase.paywall.analyticsID?.let { params["paywall_id"] = it.toAnalyticsValue() }
    purchase.paywall.analyticsName?.let { params["paywall_name"] = it.toAnalyticsValue() }
}
