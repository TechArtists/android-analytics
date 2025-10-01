//  EventAnalyticsModel.kt
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

package agency.techartists.taanalytics.models

/**
 * Type-safe wrapper for analytics event names.
 *
 * Note: "firebase_", "google_", and "ga_" prefixes are reserved by Firebase.
 * You can use spaces, snake_case or camelCase, but it's best to consult with your BI team.
 *
 * @param rawValue The event name
 * @param isInternalEvent Whether this event is tracked internally by TAAnalytics library (true)
 *                        or manually by the app (false)
 */
data class EventAnalyticsModel(
    val rawValue: String,
    internal val isInternalEvent: Boolean = false
) {
    /**
     * Creates a new event with a prefix added to the name.
     */
    fun eventByPrefixing(prefix: String): EventAnalyticsModel {
        return EventAnalyticsModel(prefix + rawValue, isInternalEvent)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EventAnalyticsModel) return false
        return rawValue == other.rawValue
    }

    override fun hashCode(): Int {
        return rawValue.hashCode()
    }
}

/**
 * Trimmed version of EventAnalyticsModel after platform-specific length restrictions are applied.
 */
data class EventAnalyticsModelTrimmed(
    val rawValue: String
)
