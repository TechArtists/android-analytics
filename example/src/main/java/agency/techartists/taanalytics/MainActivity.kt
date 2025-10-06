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

package agency.techartists.taanalytics

import agency.techartists.taanalytics.adaptor.LogcatAnalyticsAdaptor
import agency.techartists.taanalytics.compose.trackButtonTap
import agency.techartists.taanalytics.compose.trackViewShow
import agency.techartists.taanalytics.core.TAAnalytics
import agency.techartists.taanalytics.core.TAAnalyticsConfig
import agency.techartists.taanalytics.core.TAPermissionType
import agency.techartists.taanalytics.core.track
import agency.techartists.taanalytics.core.trackErrorEvent
import agency.techartists.taanalytics.core.trackPermissionButtonTap
import agency.techartists.taanalytics.core.trackPermissionScreenShow
import agency.techartists.taanalytics.core.userID
import agency.techartists.taanalytics.models.AnalyticsViewFunnelStepDetails
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.ViewAnalyticsModel
import agency.techartists.taanalytics.models.toAnalyticsValue
import agency.techartists.taanalytics.ui.theme.TAAnalyticsTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var analytics: TAAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize TAAnalytics
        val config = TAAnalyticsConfig(
            analyticsVersion = "1.0",
            adaptors = listOf(LogcatAnalyticsAdaptor()),
            sharedPreferences = getSharedPreferences("TAAnalyticsExample", MODE_PRIVATE)
        )

        analytics = TAAnalytics(applicationContext, config)

        // Start analytics in a coroutine
        // First open, app lifecycle, version updates are all tracked automatically
        lifecycleScope.launch {
            analytics.start()
        }

        enableEdgeToEdge()
        setContent {
            TAAnalyticsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExampleScreen(
                        analytics = analytics,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ExampleScreen(
    analytics: TAAnalytics,
    modifier: Modifier = Modifier
) {
    // Define view models for tracking
    val homeView = ViewAnalyticsModel("home", type = "main")
    val onboardingStep1 = ViewAnalyticsModel(
        name = "onboarding_step_1",
        type = "onboarding",
        funnelStep = AnalyticsViewFunnelStepDetails(
            funnelName = "onboarding",
            step = 1,
            isOptionalStep = false,
            isFinalStep = false
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .trackViewShow(analytics, homeView), // Track view show automatically
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TAAnalytics Phase 3 Demo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "UI Tracking with Compose",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Manual event tracking (Phase 1 & 2)
        Button(
            onClick = {
                analytics.track(
                    event = EventAnalyticsModel("custom_event"),
                    params = mapOf("source" to "manual".toAnalyticsValue())
                )
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Manual Event Track")
        }

        // Automatic button tracking with view context (Phase 3)
        Button(
            onClick = {
                trackButtonTap(analytics, "subscribe", homeView, extra = "premium_plan")
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Subscribe (Tracked)")
        }

        // Button with list index
        Button(
            onClick = {
                trackButtonTap(analytics, "select_item", homeView, index = 2)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Item #3 (List Index)")
        }

        // Funnel step tracking
        Button(
            onClick = {
                analytics.track(onboardingStep1)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Track Onboarding Step")
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Phase 4: Advanced Features",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp)
        )

        // Error tracking
        Button(
            onClick = {
                analytics.trackErrorEvent(
                    reason = "demo_error",
                    error = null,
                    extraParams = mapOf("source" to "example_button".toAnalyticsValue())
                )
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Track Error")
        }

        // Error tracking with exception
        Button(
            onClick = {
                try {
                    throw IllegalStateException("Demo exception")
                } catch (e: Exception) {
                    analytics.trackErrorEvent(
                        reason = "caught_exception",
                        error = e
                    )
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Track Error with Exception")
        }

        // Permission tracking
        Button(
            onClick = {
                analytics.trackPermissionScreenShow(TAPermissionType.PUSH_NOTIFICATIONS)
                analytics.trackPermissionButtonTap(allowed = true, TAPermissionType.PUSH_NOTIFICATIONS)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Track Permission (Allowed)")
        }

        // User ID
        Button(
            onClick = {
                analytics.userID = "demo_user_123"
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Set User ID")
        }

        // Stuck UI (with 3s timeout)
        Button(
            onClick = {
                val stuckView = ViewAnalyticsModel("stuck_demo", type = "loading")
                analytics.track(stuckView, stuckTimeout = 3000L)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Demo Stuck UI (3s)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "✅ View show tracked automatically\n" +
                    "✅ Button taps with context\n" +
                    "✅ Funnel step tracking\n" +
                    "✅ Error tracking & correction\n" +
                    "✅ Permission tracking\n" +
                    "✅ User ID sync\n" +
                    "✅ Stuck UI detection\n" +
                    "\nCheck Logcat (TAG: TAAnalytics)",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(16.dp)
        )
    }
}