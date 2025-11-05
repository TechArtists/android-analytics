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
 * Represents the type of subscription.
 */
sealed class TASubscriptionType {
    /** Trial subscription */
    object Trial : TASubscriptionType()

    /** Paid intro with pay-as-you-go pricing */
    object PaidPayAsYouGo : TASubscriptionType()

    /** Paid intro with pay-up-front pricing */
    object PaidPayUpFront : TASubscriptionType()

    /** Regular paid subscription without intro offer */
    object PaidRegular : TASubscriptionType()

    /** Other subscription type not covered by the above cases */
    data class Other(val type: String) : TASubscriptionType()

    override fun toString(): String = when (this) {
        is Trial -> "trial"
        is PaidPayAsYouGo -> "paid intro pay as you go"
        is PaidPayUpFront -> "paid intro pay up front"
        is PaidRegular -> "paid regular"
        is Other -> "other $type"
    }
}

/**
 * Interface for subscription start analytics data.
 */
interface TASubscriptionStartAnalytics {
    /** The type of subscription */
    val subscriptionType: TASubscriptionType

    /** The paywall that triggered this subscription */
    val paywall: TAPaywallAnalytics

    /** The product ID from the Play Store */
    val productID: String

    /** The price of the subscription */
    val price: Float

    /** The currency code (e.g., "USD", "EUR") */
    val currency: String
}

/**
 * Default implementation of TASubscriptionStartAnalytics.
 */
data class TASubscriptionStartAnalyticsImpl(
    override val subscriptionType: TASubscriptionType,
    override val paywall: TAPaywallAnalytics,
    override val productID: String,
    override val price: Float,
    override val currency: String
) : TASubscriptionStartAnalytics
