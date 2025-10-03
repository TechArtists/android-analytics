//  ViewAnalyticsModel.kt
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
 * Marker interface for view analytics models.
 */
interface IViewAnalyticsModel

/**
 * Details about a funnel step for tracking multi-step user flows.
 *
 * @param funnelName Name of the funnel (e.g., "onboarding", "checkout")
 * @param step Step number in the funnel (1-based)
 * @param isOptionalStep Whether this step can be skipped
 * @param isFinalStep Whether this is the last step in the funnel
 */
data class AnalyticsViewFunnelStepDetails(
    val funnelName: String,
    val step: Int,
    val isOptionalStep: Boolean = false,
    val isFinalStep: Boolean = false
)

/**
 * Represents a main view/screen for analytics tracking.
 *
 * @param name Symbolic name of the view (e.g., "home", "settings")
 * @param type Optional type/category of the view
 * @param funnelStep Optional funnel step details for multi-step flows
 */
data class ViewAnalyticsModel(
    val name: String,
    val type: String? = null,
    val funnelStep: AnalyticsViewFunnelStepDetails? = null
) : IViewAnalyticsModel {

    /**
     * Create a copy with a different type.
     */
    fun withType(type: String?): ViewAnalyticsModel {
        return copy(type = type)
    }

    /**
     * Create a copy with funnel step details.
     */
    fun withFunnelStep(funnelStep: AnalyticsViewFunnelStepDetails?): ViewAnalyticsModel {
        return copy(funnelStep = funnelStep)
    }

    companion object {
        /**
         * Convenience constructor for simple views without type or funnel.
         */
        operator fun invoke(name: String): ViewAnalyticsModel {
            return ViewAnalyticsModel(name, null, null)
        }
    }
}

/**
 * Represents a secondary/transient view shown on top of a main view.
 * Examples: dialogs, bottom sheets, popups, tooltips, labels.
 *
 * @param name Name of the secondary view
 * @param type Optional type of the secondary view
 * @param mainView The parent/main view this secondary view is shown on
 */
data class SecondaryViewAnalyticsModel(
    val name: String,
    val type: String? = null,
    val mainView: ViewAnalyticsModel
) : IViewAnalyticsModel {

    /**
     * Create a copy with a different type.
     */
    fun withType(type: String?): SecondaryViewAnalyticsModel {
        return copy(type = type)
    }
}
