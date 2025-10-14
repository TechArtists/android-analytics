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

package agency.techartists.taanalytics.compose

import agency.techartists.taanalytics.core.TAAnalytics
import agency.techartists.taanalytics.core.track
import agency.techartists.taanalytics.core.trackButtonTap
import agency.techartists.taanalytics.models.ViewAnalyticsModel
import agency.techartists.taanalytics.models.IViewAnalyticsModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Track when this composable is shown for the first time.
 *
 * Similar to iOS `onFirstAppear`, this tracks the view show event only once per composition tree.
 *
 * @param analytics TAAnalytics instance
 * @param view View model to track
 * @param stuckTimeout Optional timeout for stuck UI detection (Phase 4)
 */
fun Modifier.trackViewShow(
    analytics: TAAnalytics,
    view: ViewAnalyticsModel,
    stuckTimeout: Long? = null
): Modifier = composed {
    var hasTracked by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        if (!hasTracked) {
            analytics.track(view, stuckTimeout)
            hasTracked = true
        }

        onDispose {
            // Cleanup if needed in future phases
        }
    }

    this
}

/**
 * Track button tap with view context.
 *
 * This should be used in the onClick lambda of a Button to track taps.
 *
 * Usage:
 * ```
 * Button(
 *     onClick = { trackButtonTap(analytics, "button_name", homeView) },
 *     modifier = Modifier...
 * ) { Text("Click me") }
 * ```
 *
 * @param analytics TAAnalytics instance
 * @param buttonName Symbolic name of the button
 * @param onView View where the button is located
 * @param extra Optional extra information
 * @param index Optional 0-based index for list items
 */
fun trackButtonTap(
    analytics: TAAnalytics,
    buttonName: String,
    onView: IViewAnalyticsModel,
    extra: String? = null,
    index: Int? = null
) {
    analytics.trackButtonTap(
        buttonName,
        onView,
        extra,
        index
    )
}

/**
 * Run an action only on first composition.
 *
 * Similar to iOS `onFirstAppear` modifier.
 *
 * @param action Action to run on first composition
 */
@Composable
fun OnFirstComposition(action: () -> Unit) {
    var hasRun by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasRun) {
            action()
            hasRun = true
        }
    }
}

/**
 * Modifier version of OnFirstComposition.
 */
fun Modifier.onFirstComposition(action: () -> Unit): Modifier = composed {
    var hasRun by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasRun) {
            action()
            hasRun = true
        }
    }

    this
}
