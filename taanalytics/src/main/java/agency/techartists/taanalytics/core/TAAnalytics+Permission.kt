//  TAAnalytics+Permission.kt
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

import agency.techartists.taanalytics.models.ViewAnalyticsModel

/**
 * Represents different types of permissions that can be requested.
 */
enum class TAPermissionType(val value: String) {
    /** Push notification permission */
    PUSH_NOTIFICATIONS("push_notifications"),

    /** Location permission */
    LOCATION("location"),

    /** Camera permission */
    CAMERA("camera"),

    /** Microphone/audio recording permission */
    MICROPHONE("microphone"),

    /** Storage/files permission */
    STORAGE("storage"),

    /** Contacts permission */
    CONTACTS("contacts"),

    /** Calendar permission */
    CALENDAR("calendar"),

    /** Phone permission */
    PHONE("phone"),

    /** SMS permission */
    SMS("sms"),

    /** Custom permission type */
    CUSTOM("");

    override fun toString(): String = value

    companion object {
        /**
         * Create a custom permission type with a specific value.
         */
        fun custom(value: String): String = value
    }
}

/**
 * Track when a permission request screen is shown.
 *
 * Sends a `ui_view_show` event with name="permission" and type=permissionType.
 *
 * @param permissionType The type of permission being requested
 */
fun TAAnalytics.trackPermissionScreenShow(permissionType: TAPermissionType) {
    val view = ViewAnalyticsModel(
        name = "permission",
        type = permissionType.toString()
    )
    track(view)
}

/**
 * Track when a permission request screen is shown (custom type).
 *
 * @param customPermissionType Custom permission type identifier
 */
fun TAAnalytics.trackPermissionScreenShow(customPermissionType: String) {
    val view = ViewAnalyticsModel(
        name = "permission",
        type = customPermissionType
    )
    track(view)
}

/**
 * Track a permission button tap (allow/deny).
 *
 * Sends a `ui_button_tap` event with:
 * - name: "allow" or "dont_allow"
 * - view_name: "permission"
 * - view_type: permissionType
 *
 * @param allowed Whether the user allowed or denied the permission
 * @param permissionType The type of permission
 */
fun TAAnalytics.trackPermissionButtonTap(
    allowed: Boolean,
    permissionType: TAPermissionType
) {
    val view = ViewAnalyticsModel(
        name = "permission",
        type = permissionType.toString()
    )
    trackButtonTap(
        symbolicName = if (allowed) "allow" else "dont_allow",
        onView = view
    )
}

/**
 * Track a permission button tap with custom status.
 *
 * Sends a `ui_button_tap` event with:
 * - name: status (e.g., "already_granted", "settings_opened")
 * - view_name: "permission"
 * - view_type: permissionType
 *
 * @param status The custom status to track
 * @param permissionType The type of permission
 */
fun TAAnalytics.trackPermissionButtonTap(
    status: String,
    permissionType: TAPermissionType
) {
    val view = ViewAnalyticsModel(
        name = "permission",
        type = permissionType.toString()
    )
    trackButtonTap(
        symbolicName = status,
        onView = view
    )
}

/**
 * Track a permission button tap (custom type).
 *
 * @param allowed Whether the user allowed or denied the permission
 * @param customPermissionType Custom permission type identifier
 */
fun TAAnalytics.trackPermissionButtonTap(
    allowed: Boolean,
    customPermissionType: String
) {
    val view = ViewAnalyticsModel(
        name = "permission",
        type = customPermissionType
    )
    trackButtonTap(
        symbolicName = if (allowed) "allow" else "dont_allow",
        onView = view
    )
}
