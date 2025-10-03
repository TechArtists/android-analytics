//  EventBuffer.kt
//
//  Copyright (c) 2024 Tech Artists Agency SRL
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
//

package agency.techartists.taanalytics.core

import agency.techartists.taanalytics.adaptor.AnalyticsAdaptor
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import agency.techartists.taanalytics.models.EventAnalyticsModel
import android.util.Log
import java.util.LinkedList
import java.util.Queue

/**
 * Represents a queued event waiting to be sent to adaptors.
 */
data class DeferredQueuedEvent(
    val event: EventAnalyticsModel,
    val dateAdded: Long = System.currentTimeMillis(),
    val parameters: Map<String, AnalyticsBaseParameterValue>?
)

/**
 * Buffers events until adaptors are initialized, then flushes them.
 * This ensures no events are lost during adaptor startup.
 */
class EventBuffer {
    companion object {
        private const val TAG = "TAAnalytics.EventBuffer"
    }

    private val eventQueue: Queue<DeferredQueuedEvent> = LinkedList()
    private var startedAdaptors: List<AnalyticsAdaptor> = emptyList()
    private var isReady = false

    /**
     * Add an event to track. If adaptors are ready, tracks immediately.
     * Otherwise, queues for later.
     */
    fun addEvent(
        event: EventAnalyticsModel,
        params: Map<String, AnalyticsBaseParameterValue>? = null
    ) {
        if (isReady && startedAdaptors.isNotEmpty()) {
            trackEventInStartedAdaptors(event, params)
        } else {
            eventQueue.offer(DeferredQueuedEvent(event, System.currentTimeMillis(), params))
            Log.d(TAG, "Event '${event.rawValue}' queued (${eventQueue.size} events in queue)")
        }
    }

    /**
     * Set up the adaptors and flush any queued events.
     */
    fun setupAdaptors(adaptors: List<AnalyticsAdaptor>) {
        this.startedAdaptors = adaptors
        this.isReady = true
        flushDeferredEventQueue()
    }

    /**
     * Get the list of started adaptors.
     */
    fun getStartedAdaptors(): List<AnalyticsAdaptor> = startedAdaptors

    /**
     * Track an event in all started adaptors.
     */
    private fun trackEventInStartedAdaptors(
        event: EventAnalyticsModel,
        params: Map<String, AnalyticsBaseParameterValue>?
    ) {
        startedAdaptors.forEach { adaptor ->
            try {
                adaptor.track(adaptor.trim(event), params)

                val eventName = adaptor.trim(event).rawValue
                val adaptorName = adaptor::class.simpleName

                // logging
                if (!params.isNullOrEmpty()) {
                    val paramsString = params.toList()
                        .sortedBy { it.first }
                        .joinToString(", ") { "${it.first}:${it.second}" }
                    Log.i(TAG, "Adaptor '$adaptorName' logged event: '$eventName', params: [$paramsString]")
                } else {
                    Log.i(TAG, "Adaptor '$adaptorName' logged event: '$eventName'")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error tracking event in ${adaptor::class.simpleName}: ${e.message}", e)
            }
        }
    }

    /**
     * Flush all queued events to the started adaptors.
     */
    private fun flushDeferredEventQueue() {
        if (eventQueue.isEmpty()) {
            Log.d(TAG, "No events to flush")
            return
        }

        Log.i(TAG, "Flushing ${eventQueue.size} queued events")

        while (eventQueue.isNotEmpty()) {
            val deferredEvent = eventQueue.poll() ?: break

            // Add timeDelta parameter to show how long the event was queued
            val timeDelta = System.currentTimeMillis() - deferredEvent.dateAdded
            val paramsWithTimeDelta = (deferredEvent.parameters ?: emptyMap()).toMutableMap().apply {
                put("timeDelta", object : AnalyticsBaseParameterValue {
                    override fun toAnalyticsValue(): Any = timeDelta
                    override fun toString(): String = timeDelta.toString()
                })
            }

            trackEventInStartedAdaptors(deferredEvent.event, paramsWithTimeDelta)
        }

        Log.i(TAG, "Event queue flushed")
    }
}
