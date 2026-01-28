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

package agency.techartists.taanalytics.constants

import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel

/**
 * Standard analytics events used by TAAnalytics.
 * These are marked as internal events and will be prefixed according to config.
 */
object Events {
    /** Sent on first app open ever */
    val OUR_FIRST_OPEN = EventAnalyticsModel("our_first_open", isInternalEvent = true)

    /** Generic UI view shown event */
    val UI_VIEW_SHOW = EventAnalyticsModel("ui_view_show")

    /** Generic UI button tap event */
    val UI_BUTTON_TAP = EventAnalyticsModel("ui_button_tap")

    /** Sent when app goes to foreground. Has `is_cold_launch` boolean parameter */
    val APP_OPEN = EventAnalyticsModel("app_open", isInternalEvent = true)

    /** Sent when app goes to background */
    val APP_CLOSE = EventAnalyticsModel("app_close", isInternalEvent = true)

    /** Generic error event. Parameters: `reason` (mandatory), optionally error details */
    val ERROR = EventAnalyticsModel("error", isInternalEvent = true)

    /** Error corrected event. Parameters: `reason` (mandatory), optionally error details */
    val ERROR_CORRECTED = EventAnalyticsModel("error_corrected", isInternalEvent = true)

    /** App version updated. Parameters: `from_version`, `to_version`, `from_build`, `to_build` */
    val APP_VERSION_UPDATE = EventAnalyticsModel("app_version_update", isInternalEvent = true)

    /** OS version updated. Parameters: `from_version`, `to_version` */
    val OS_VERSION_UPDATE = EventAnalyticsModel("os_version_update", isInternalEvent = true)

    /** User engagement event. Parameters: `name` */
    val ENGAGEMENT = EventAnalyticsModel("engagement", isInternalEvent = true)

    /** Primary engagement of the app. Sent alongside ENGAGEMENT with same parameters */
    val ENGAGEMENT_PRIMARY = EventAnalyticsModel("engagement_primary", isInternalEvent = true)

    /** Onboarding started */
    val ONBOARDING_ENTER = EventAnalyticsModel("onboarding_enter", isInternalEvent = true)

    /** Onboarding finished */
    val ONBOARDING_EXIT = EventAnalyticsModel("onboarding_exit", isInternalEvent = true)

    /** Account signup started */
    val ACCOUNT_SIGNUP_ENTER = EventAnalyticsModel("account_signup_enter", isInternalEvent = true)

    /** Account signup finished */
    val ACCOUNT_SIGNUP_EXIT = EventAnalyticsModel("account_signup_exit", isInternalEvent = true)

    /** Paywall shown. Parameters: `placement`, `id` (optional) */
    val PAYWALL_ENTER = EventAnalyticsModel("paywall_enter", isInternalEvent = true)

    /** Paywall dismissed. Parameters: `placement`, `id` (optional) */
    val PAYWALL_EXIT = EventAnalyticsModel("paywall_exit", isInternalEvent = true)

    /** Purchase button tapped on paywall. Parameters: `button_name`, `product_id`, `paywall_placement`, `paywall_id` */
    val PAYWALL_PURCHASE_TAP = EventAnalyticsModel("paywall_purchase_tap", isInternalEvent = true)

    /** Subscription started with intro offer */
    val SUBSCRIPTION_START_INTRO = EventAnalyticsModel("subscription_start_intro", isInternalEvent = true)

    /** Subscription started with regular paid price */
    val SUBSCRIPTION_START_PAID_REGULAR = EventAnalyticsModel("subscription_start_paid_regular", isInternalEvent = true)

    /** New subscription started */
    val SUBSCRIPTION_START_NEW = EventAnalyticsModel("subscription_start_new", isInternalEvent = true)

    /** Subscription restored */
    val SUBSCRIPTION_RESTORE = EventAnalyticsModel("subscription_restore", isInternalEvent = true)

    /** One-time non-consumable purchase completed */
    val PURCHASE_NON_CONSUMABLE_ONE_TIME = EventAnalyticsModel("purchase_non_consumable_one_time", isInternalEvent = true)

    /** Consumable purchase completed */
    val PURCHASE_CONSUMABLE = EventAnalyticsModel("purchase_consumable", isInternalEvent = true)

    /** New purchase completed */
    val PURCHASE_NEW = EventAnalyticsModel("purchase_new", isInternalEvent = true)
}

/**
 * Standard user properties used by TAAnalytics.
 */
object UserProperties {
    /** Version of analytics schema */
    val ANALYTICS_VERSION = UserPropertyAnalyticsModel("analytics_version", isInternalUserProperty = true)

    /** Date of app installation (ISO 8601 format: YYYY-MM-DD) */
    val INSTALL_DATE = UserPropertyAnalyticsModel("install_date", isInternalUserProperty = true)

    /** App version at install time */
    val INSTALL_VERSION = UserPropertyAnalyticsModel("install_version", isInternalUserProperty = true)

    /** OS version at install time */
    val INSTALL_OS_VERSION = UserPropertyAnalyticsModel("install_os_version", isInternalUserProperty = true)

    /** Whether device is rooted at install time */
    val INSTALL_IS_ROOTED = UserPropertyAnalyticsModel("install_is_rooted", isInternalUserProperty = true)

    /** UI theme/appearance at install time (dark/light) */
    val INSTALL_UI_APPEARANCE = UserPropertyAnalyticsModel("install_ui_appearance", isInternalUserProperty = true)

    /** Ever-increasing counter on each cold app launch, starting from 1 at first open */
    val APP_COLD_LAUNCH_COUNT = UserPropertyAnalyticsModel("app_cold_launch_count", isInternalUserProperty = true)

    /** Ever-increasing counter on each app open, starting from 1 at first open */
    val APP_OPEN_COUNT = UserPropertyAnalyticsModel("app_open_count", isInternalUserProperty = true)

    /** Last view shown (parent view only) */
    val LAST_VIEW_SHOW = UserPropertyAnalyticsModel("last_view_show", isInternalUserProperty = true)

    /** Current active subscription product identifier */
    val SUBSCRIPTION = UserPropertyAnalyticsModel("subscription", isInternalUserProperty = true)

    /** Secondary subscription product identifier (for apps with multiple concurrent subscriptions) */
    val SUBSCRIPTION2 = UserPropertyAnalyticsModel("subscription2", isInternalUserProperty = true)

    /** Introductory offer type: "trial", "pay as you go", or "pay up front" */
    val SUBSCRIPTION_INTRO_OFFER = UserPropertyAnalyticsModel("subscription_intro_offer", isInternalUserProperty = true)
}
