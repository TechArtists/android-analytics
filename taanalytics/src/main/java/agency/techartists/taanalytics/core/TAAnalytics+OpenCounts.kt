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

import agency.techartists.taanalytics.constants.Events
import agency.techartists.taanalytics.constants.UserProperties
import agency.techartists.taanalytics.models.AnalyticsBaseParameterValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * The number of cold launches (process starts) for this app.
 * The very first launch has a count of 1.
 */
val TAAnalytics.coldLaunchCount: Int
    get() = get(UserProperties.APP_COLD_LAUNCH_COUNT)?.toIntOrNull() ?: 0

/**
 * The number of days since the app was installed, based on 24-hour periods.
 *
 * Age 0 means it's within the first 24 hours of being installed.
 *
 * This calculates relative time (elapsed 24-hour periods) rather than calendar days.
 * Use [installAgeLocalizedCalendarDays] for calendar day calculations.
 *
 * @return Number of 24-hour periods since install, or null if install date is not available
 */
val TAAnalytics.installAgeRelativeDays: Int?
    get() {
        val installDateString = get(UserProperties.INSTALL_DATE) ?: return null
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val installDate = dateFormat.parse(installDateString) ?: return null
            relativeAgeBetween(installDate, Date())
        } catch (e: Exception) {
            null
        }
    }

/**
 * The number of calendar days since the app was installed, in the device's local timezone.
 *
 * Age 0 means it's the same calendar day as install.
 * If a user installs the app at 23:59:59, one minute later this value will be 1.
 *
 * This uses calendar day boundaries in the local timezone, so it can differ from
 * [installAgeRelativeDays] which uses 24-hour periods.
 *
 * @return Number of calendar days since install, or null if install date is not available
 */
val TAAnalytics.installAgeLocalizedCalendarDays: Int?
    get() {
        val installDateString = get(UserProperties.INSTALL_DATE) ?: return null
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val installDate = dateFormat.parse(installDateString) ?: return null
            calendarAgeBetween(installDate, Date(), TimeZone.getDefault())
        } catch (e: Exception) {
            null
        }
    }

/**
 * Track the first open event if this is the very first app open.
 *
 * Sends an `our_first_open` event with optional custom parameters.
 * This event is only sent once per lifetime and only on the first cold launch.
 *
 * @param paramsCallback Optional callback to provide additional parameters for the event.
 *                      The callback is only invoked if this is the first open, to avoid
 *                      calculating expensive parameters unnecessarily.
 * @return true if the first open event was sent, false otherwise
 */
fun TAAnalytics.maybeTrackTAFirstOpen(
    paramsCallback: (() -> Map<String, AnalyticsBaseParameterValue>?)? = null
): Boolean {
    return if (isFirstOpen) {
        val params = paramsCallback?.invoke()
        track(
            event = Events.OUR_FIRST_OPEN,
            params = params,
            logCondition = EventLogCondition.LOG_ONLY_ONCE_PER_LIFETIME
        )
        true
    } else {
        false
    }
}

/**
 * Calculate the number of complete 24-hour periods between two dates.
 *
 * @param startDate The start date
 * @param endDate The end date
 * @return Number of complete 24-hour periods
 */
internal fun relativeAgeBetween(startDate: Date, endDate: Date): Int {
    val timePassed = endDate.time - startDate.time
    val daysPassed = TimeUnit.MILLISECONDS.toDays(timePassed).toInt()
    return daysPassed
}

/**
 * Calculate the number of calendar days between two dates in the given timezone.
 *
 * This uses the start of each day in the specified timezone.
 *
 * @param startDate The start date
 * @param endDate The end date
 * @param timeZone The timezone to use for calendar calculations
 * @return Number of calendar days, or null if calculation fails
 */
internal fun calendarAgeBetween(startDate: Date, endDate: Date, timeZone: TimeZone): Int? {
    return try {
        val calendar = Calendar.getInstance(timeZone)

        // Get start of day for start date
        calendar.time = startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDateDayStart = calendar.time

        // Get start of day for end date
        calendar.time = endDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val endDateDayStart = calendar.time

        // Calculate difference in days
        val diffInMillis = endDateDayStart.time - startDateDayStart.time
        val daysDiff = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        daysDiff
    } catch (e: Exception) {
        null
    }
}
