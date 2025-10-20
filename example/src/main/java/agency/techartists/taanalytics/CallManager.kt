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

package agency.techartists.taanalytics

import agency.techartists.taanalytics.core.TAAnalytics
import agency.techartists.taanalytics.core.trackErrorEvent
import agency.techartists.taanalytics.models.toAnalyticsValue
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * CallManager demonstrates how to integrate TAAnalytics into business logic classes.
 *
 * This class handles phone call functionality with analytics tracking,
 * showing best practices for:
 * - Dependency injection of analytics
 * - PII-safe parameter logging
 * - Error tracking for validation failures
 */
class CallManager(
    private val analytics: TAAnalytics,
    private val context: Context
) {

    /**
     * Attempts to place a call to the given phone number.
     *
     * This method will:
     * - Validate that the phone number has at least 4 digits
     * - Track a CALL_PLACED event with the last 4 digits (PII protection)
     * - Launch the phone dialer with the number
     * - Track an error event if the number is invalid
     *
     * @param phoneNumber The phone number to call
     */
    fun callIfAtLeastFourDigits(phoneNumber: String) {
        val digitsOnly = phoneNumber.filter { it.isDigit() }

        if (digitsOnly.length >= 4) {
            // Track call placed with only the last 4 digits to protect PII
            analytics.track(
                event = CustomEvents.CALL_PLACED,
                params = mapOf(
                    "phone_number_suffix" to digitsOnly.takeLast(4).toAnalyticsValue()
                )
            )

            // Actually place the call by launching the dialer
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                analytics.trackErrorEvent(
                    reason = "call_intent_failed",
                    error = e
                )
            }
        } else {
            // Track error for invalid phone number
            analytics.trackErrorEvent(
                reason = "call_not_placed",
                extraParams = mapOf(
                    "reason" to "not enough digits".toAnalyticsValue(),
                    "digits" to digitsOnly.length.toAnalyticsValue()
                )
            )
        }
    }
}
