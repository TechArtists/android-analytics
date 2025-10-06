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
import agency.techartists.taanalytics.models.SecondaryViewAnalyticsModel
import agency.techartists.taanalytics.models.ViewAnalyticsModel
import agency.techartists.taanalytics.models.IViewAnalyticsModel
import agency.techartists.taanalytics.models.toAnalyticsValue

/**
 * Track a main view/screen show event.
 *
 * Sends a `ui_view_show` event with view details and updates the `last_view_show` user property.
 *
 * Parameters sent:
 * - name: String
 * - type: String? (if provided)
 * - funnel_name: String? (if funnel step provided)
 * - funnel_step: Int? (if funnel step provided)
 * - funnel_step_is_optional: Boolean? (if funnel step provided)
 * - funnel_step_is_final: Boolean? (if funnel step provided)
 *
 * @param view The view that was shown
 * @param stuckTimeout Optional timeout in milliseconds for stuck UI detection (Phase 4)
 */
fun TAAnalytics.track(view: ViewAnalyticsModel, stuckTimeout: Long? = null) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

    // Add view parameters
    addViewParameters(view, params, prefix = "")

    // Cancel any existing stuck UI manager and track correction if needed
    stuckUIManager?.trackCorrectedIfNeeded()
    stuckUIManager?.cancel()
    stuckUIManager = null

    // Create new stuck UI manager if timeout specified
    if (stuckTimeout != null && stuckTimeout > 0) {
        stuckUIManager = StuckUIManager(params, stuckTimeout, this)
    }

    // Update last view shown
    lastViewShow = view
    set(UserProperties.LAST_VIEW_SHOW, formatLastViewShow(view))

    // Track the event
    track(
        event = Events.UI_VIEW_SHOW,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Track a secondary/transient view show event.
 *
 * Sends a `ui_view_show` event with both secondary and main view details.
 *
 * Parameters sent:
 * - secondary_view_name: String
 * - secondary_view_type: String? (if provided)
 * - name, type, funnel_* from main view
 *
 * @param secondaryView The secondary view that was shown
 */
fun TAAnalytics.track(secondaryView: SecondaryViewAnalyticsModel) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

    // Add secondary view parameters
    params["secondary_view_name"] = secondaryView.name.toAnalyticsValue()
    secondaryView.type?.let { params["secondary_view_type"] = it.toAnalyticsValue() }

    // Add main view parameters
    addViewParameters(secondaryView.mainView, params, prefix = "")

    // Track the event
    track(
        event = Events.UI_VIEW_SHOW,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Track a button tap event.
 *
 * Sends a `ui_button_tap` event with button details and view context.
 *
 * Parameters sent:
 * - name: String (button name)
 * - extra: String? (if provided)
 * - order: Int? (1-based, if index provided)
 * - view_{name, type, funnel_*}: from parent view (prefixed with "view_")
 * - secondary_view_{name, type}: if tapped on secondary view
 *
 * @param symbolicName Symbolic name of the button (not localized text)
 * @param onView The view where the button was tapped
 * @param extra Optional extra information (e.g., selected item, plan ID)
 * @param index Optional 0-based index (will be sent as 1-based "order")
 */
fun TAAnalytics.trackButtonTap(
    symbolicName: String,
    onView: IViewAnalyticsModel,
    extra: String? = null,
    index: Int? = null
) {
    val params = mutableMapOf<String, AnalyticsBaseParameterValue>()

    // Add button parameters
    params["name"] = symbolicName.toAnalyticsValue()
    index?.let { params["order"] = (it + 1).toAnalyticsValue() } // Convert to 1-based
    extra?.let { params["extra"] = it.toAnalyticsValue() }

    // Add view context parameters
    when (onView) {
        is ViewAnalyticsModel -> {
            addViewParameters(onView, params, prefix = "view_")
        }
        is SecondaryViewAnalyticsModel -> {
            params["secondary_view_name"] = onView.name.toAnalyticsValue()
            onView.type?.let { params["secondary_view_type"] = it.toAnalyticsValue() }
            addViewParameters(onView.mainView, params, prefix = "view_")
        }
    }

    // Track the event
    track(
        event = Events.UI_BUTTON_TAP,
        params = params,
        logCondition = EventLogCondition.LOG_ALWAYS
    )
}

/**
 * Add view parameters to a map with optional prefix.
 *
 * @param view The view to extract parameters from
 * @param params The map to add parameters to
 * @param prefix Prefix for parameter names (e.g., "view_")
 */
private fun addViewParameters(
    view: ViewAnalyticsModel,
    params: MutableMap<String, AnalyticsBaseParameterValue>,
    prefix: String
) {
    params["${prefix}name"] = view.name.toAnalyticsValue()
    view.type?.let { params["${prefix}type"] = it.toAnalyticsValue() }

    view.funnelStep?.let { funnel ->
        params["${prefix}funnel_name"] = funnel.funnelName.toAnalyticsValue()
        params["${prefix}funnel_step"] = funnel.step.toAnalyticsValue()
        params["${prefix}funnel_step_is_optional"] = funnel.isOptionalStep.toAnalyticsValue()
        params["${prefix}funnel_step_is_final"] = funnel.isFinalStep.toAnalyticsValue()
    }
}

/**
 * Format last view show for user property storage.
 * Format: "name;type;funnel_name;step;is_optional;is_final"
 */
private fun formatLastViewShow(view: ViewAnalyticsModel): String {
    return buildString {
        append(view.name)
        append(";")
        append(view.type ?: "")
        append(";")
        append(view.funnelStep?.funnelName ?: "")
        append(";")
        append(view.funnelStep?.step?.toString() ?: "")
        append(";")
        append(view.funnelStep?.isOptionalStep?.toString() ?: "")
        append(";")
        append(view.funnelStep?.isFinalStep?.toString() ?: "")
    }
}

/**
 * Get the last view that was tracked.
 */
var TAAnalytics.lastViewShow: ViewAnalyticsModel?
    get() {
        val formatted = get(UserProperties.LAST_VIEW_SHOW) ?: return null
        return parseLastViewShow(formatted)
    }
    set(value) {
        // Setter is handled in track(view) method
    }

/**
 * Parse last view show from formatted string.
 */
private fun parseLastViewShow(formatted: String): ViewAnalyticsModel? {
    val parts = formatted.split(";")
    if (parts.size < 6) return null

    val name = parts[0].takeIf { it.isNotEmpty() } ?: return null
    val type = parts[1].takeIf { it.isNotEmpty() }
    val funnelName = parts[2].takeIf { it.isNotEmpty() }
    val step = parts[3].toIntOrNull()
    val isOptional = parts[4].toBooleanStrictOrNull()
    val isFinal = parts[5].toBooleanStrictOrNull()

    val funnelStep = if (funnelName != null && step != null && isOptional != null && isFinal != null) {
        agency.techartists.taanalytics.models.AnalyticsViewFunnelStepDetails(
            funnelName = funnelName,
            step = step,
            isOptionalStep = isOptional,
            isFinalStep = isFinal
        )
    } else null

    return ViewAnalyticsModel(
        name = name,
        type = type,
        funnelStep = funnelStep
    )
}
