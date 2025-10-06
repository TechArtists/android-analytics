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

import agency.techartists.taanalytics.adaptor.AnalyticsAdaptorWithReadOnlyUserPseudoID
import agency.techartists.taanalytics.adaptor.AnalyticsAdaptorWithReadWriteUserID
import agency.techartists.taanalytics.adaptor.AnalyticsAdaptorWithWriteOnlyUserID

/**
 * Get the platform-generated user pseudo ID.
 *
 * This is typically an automatically generated ID by the analytics platform
 * (e.g., Firebase App Instance ID).
 *
 * @return The pseudo ID from the first adaptor that provides one, or null if none available
 */
val TAAnalytics.userPseudoID: String?
    get() {
        val startedAdaptors = (this::class.java.getDeclaredField("eventBuffer")
            .apply { isAccessible = true }
            .get(this) as EventBuffer)
            .getStartedAdaptors()

        return startedAdaptors
            .filterIsInstance<AnalyticsAdaptorWithReadOnlyUserPseudoID>()
            .firstOrNull()
            ?.getUserPseudoID()
    }

/**
 * Get or set the user ID across all analytics adaptors.
 *
 * Getting: Returns the user ID from the first adaptor that supports reading it.
 * Setting: Propagates the user ID to all adaptors that support writing it.
 *
 * Use this to associate analytics events with your app's user identifier.
 */
var TAAnalytics.userID: String?
    get() {
        val startedAdaptors = (this::class.java.getDeclaredField("eventBuffer")
            .apply { isAccessible = true }
            .get(this) as EventBuffer)
            .getStartedAdaptors()

        return startedAdaptors
            .filterIsInstance<AnalyticsAdaptorWithReadWriteUserID>()
            .firstOrNull()
            ?.getUserID()
    }
    set(value) {
        val startedAdaptors = (this::class.java.getDeclaredField("eventBuffer")
            .apply { isAccessible = true }
            .get(this) as EventBuffer)
            .getStartedAdaptors()

        // Set user ID on all write-only adaptors
        startedAdaptors
            .filterIsInstance<AnalyticsAdaptorWithWriteOnlyUserID>()
            .forEach { it.setUserID(value) }

        // Set user ID on all read-write adaptors
        startedAdaptors
            .filterIsInstance<AnalyticsAdaptorWithReadWriteUserID>()
            .forEach { it.setUserID(value) }
    }
