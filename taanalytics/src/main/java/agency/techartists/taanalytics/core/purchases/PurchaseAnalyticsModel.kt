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

/**
 * Represents the type of in-app purchase.
 */
sealed class TAPurchaseType {
    /** One-time non-consumable purchase */
    object NonConsumableOneTime : TAPurchaseType()

    /** Consumable purchase */
    object Consumable : TAPurchaseType()

    /** Other purchase type not covered by the above cases */
    data class Other(val type: String) : TAPurchaseType()

    override fun toString(): String = when (this) {
        is NonConsumableOneTime -> "non consumable one time"
        is Consumable -> "consumable"
        is Other -> "other $type"
    }
}

/**
 * Interface for purchase analytics data.
 */
interface TAPurchaseAnalytics {
    /** The type of purchase */
    val purchaseType: TAPurchaseType

    /** The paywall that triggered this purchase */
    val paywall: TAPaywallAnalytics

    /** The product ID from the Play Store */
    val productID: String

    /** The price of the purchase */
    val price: Float

    /** The currency code (e.g., "USD", "EUR") */
    val currency: String
}

/**
 * Default implementation of TAPurchaseAnalytics.
 */
data class TAPurchaseAnalyticsImpl(
    override val purchaseType: TAPurchaseType,
    override val paywall: TAPaywallAnalytics,
    override val productID: String,
    override val price: Float,
    override val currency: String
) : TAPurchaseAnalytics
