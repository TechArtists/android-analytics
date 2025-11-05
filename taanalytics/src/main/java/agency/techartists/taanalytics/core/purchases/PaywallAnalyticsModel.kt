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
 * Represents the reason for exiting a paywall.
 */
sealed class TAPaywallExitReason {
    /** User closed the paywall without making a purchase */
    object ClosedPaywall : TAPaywallExitReason()

    /** User cancelled during payment confirmation */
    object CancelledPaymentConfirmation : TAPaywallExitReason()

    /** User completed a new subscription purchase */
    object NewSubscription : TAPaywallExitReason()

    /** User restored a previous subscription */
    object RestoredSubscription : TAPaywallExitReason()

    /** Other reason not covered by the above cases */
    data class Other(val reason: String) : TAPaywallExitReason()

    override fun toString(): String = when (this) {
        is ClosedPaywall -> "closed paywall"
        is CancelledPaymentConfirmation -> "cancelled payment confirmation"
        is NewSubscription -> "new subscription"
        is RestoredSubscription -> "restored subscription"
        is Other -> "other $reason"
    }
}

/**
 * Interface for paywall analytics data.
 */
interface TAPaywallAnalytics {
    /** The placement that triggered the paywall */
    val analyticsPlacement: String

    /** The ID of the paywall, optional. For example, you might have 2 different types of paywalls
     *  that are shown in an A/B test, each with their own ID. */
    val analyticsID: String?

    /** The name of the paywall, optional. It needs to be paired with the ID,
     *  but it's usually more human readable */
    val analyticsName: String?
}

/**
 * Default implementation of TAPaywallAnalytics.
 */
data class TAPaywallAnalyticsImpl(
    override val analyticsPlacement: String,
    override val analyticsID: String? = null,
    override val analyticsName: String? = null
) : TAPaywallAnalytics
