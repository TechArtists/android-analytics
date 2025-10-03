package agency.techartists.taanalytics

import agency.techartists.taanalytics.adaptor.LogcatAnalyticsAdaptor
import agency.techartists.taanalytics.compose.trackButtonTap
import agency.techartists.taanalytics.compose.trackViewShow
import agency.techartists.taanalytics.core.TAAnalytics
import agency.techartists.taanalytics.core.TAAnalyticsConfig
import agency.techartists.taanalytics.core.track
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

        Text(
            text = "✅ View show tracked automatically\n" +
                    "✅ Button taps with context\n" +
                    "✅ Funnel step tracking\n" +
                    "\nCheck Logcat (TAG: TAAnalytics)",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(16.dp)
        )
    }
}