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

import agency.techartists.taanalytics.adaptor.AnalyticsAdaptor
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import android.util.Log

/**
 * Buffers user properties until adaptors are initialized, then flushes them.
 * This ensures no user properties are lost during adaptor startup.
 */
class UserPropertyBuffer {
    companion object {
        private const val TAG = "TAAnalytics.UserPropertyBuffer"
    }

    private val propertyQueue: MutableMap<UserPropertyAnalyticsModel, String?> = mutableMapOf()
    private var startedAdaptors: List<AnalyticsAdaptor> = emptyList()
    private var isReady = false

    /**
     * Set a user property. If adaptors are ready, sets immediately.
     * Otherwise, queues for later. If the same property is set multiple times
     * before adaptors are ready, only the last value is kept.
     */
    fun setProperty(
        userProperty: UserPropertyAnalyticsModel,
        value: String?
    ) {
        if (isReady && startedAdaptors.isNotEmpty()) {
            setPropertyInStartedAdaptors(userProperty, value)
        } else {
            propertyQueue[userProperty] = value
            Log.d(TAG, "User property '${userProperty.rawValue}' queued (${propertyQueue.size} properties in queue)")
        }
    }

    /**
     * Set up the adaptors and flush any queued user properties.
     */
    fun setupAdaptors(adaptors: List<AnalyticsAdaptor>) {
        this.startedAdaptors = adaptors
        this.isReady = true
        flushDeferredPropertyQueue()
    }

    /**
     * Set a user property in all started adaptors.
     */
    private fun setPropertyInStartedAdaptors(
        userProperty: UserPropertyAnalyticsModel,
        value: String?
    ) {
        startedAdaptors.forEach { adaptor ->
            try {
                adaptor.set(adaptor.trim(userProperty), value)

                val propertyName = adaptor.trim(userProperty).rawValue
                val adaptorName = adaptor::class.simpleName

                Log.i(TAG, "Adaptor '$adaptorName' set user property: '$propertyName' = $value")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting user property in ${adaptor::class.simpleName}: ${e.message}", e)
            }
        }
    }

    /**
     * Flush all queued user properties to the started adaptors.
     */
    private fun flushDeferredPropertyQueue() {
        if (propertyQueue.isEmpty()) {
            Log.d(TAG, "No user properties to flush")
            return
        }

        Log.i(TAG, "Flushing ${propertyQueue.size} queued user properties")

        propertyQueue.forEach { (userProperty, value) ->
            setPropertyInStartedAdaptors(userProperty, value)
        }

        propertyQueue.clear()
        Log.i(TAG, "User property queue flushed")
    }
}
