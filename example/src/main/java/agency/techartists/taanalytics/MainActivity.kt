package agency.techartists.taanalytics

import agency.techartists.taanalytics.adaptor.LogcatAnalyticsAdaptor
import agency.techartists.taanalytics.constants.Events
import agency.techartists.taanalytics.core.EventLogCondition
import agency.techartists.taanalytics.core.TAAnalytics
import agency.techartists.taanalytics.core.TAAnalyticsConfig
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.toAnalyticsValue
import agency.techartists.taanalytics.ui.theme.TAAnalyticsTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
        lifecycleScope.launch {
            analytics.start()

            // Track first open (only once ever)
            analytics.track(
                event = Events.OUR_FIRST_OPEN,
                params = mapOf(
                    "source" to "onCreate".toAnalyticsValue()
                ),
                logCondition = EventLogCondition.LOG_ONLY_ONCE_PER_LIFETIME
            )
        }

        enableEdgeToEdge()
        setContent {
            TAAnalyticsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExampleScreen(
                        modifier = Modifier.padding(innerPadding),
                        onEventClick = { trackExampleEvent() },
                        onEventOncePerSessionClick = { trackExampleEvent(name = "once per session", EventLogCondition.LOG_ONLY_ONCE_PER_APP_SESSION) },
                        onEventOncePerLifetimeClick = { trackExampleEvent(name = "once per lifetime", EventLogCondition.LOG_ONLY_ONCE_PER_LIFETIME) },
                        onUserPropertyClick = { setExampleUserProperty() }
                    )
                }
            }
        }
    }

    private fun trackExampleEvent(name: String = "example", logCondition: EventLogCondition = EventLogCondition.LOG_ALWAYS) {
        // Track a custom event with parameters
        analytics.track(
            event = EventAnalyticsModel("button_clicked"),
            params = mapOf(
                "button_name" to name.toAnalyticsValue(),
                "timestamp" to System.currentTimeMillis().toAnalyticsValue()
            ),
            logCondition = logCondition
        )
    }

    private fun setExampleUserProperty() {
        // Set a user property
        analytics.set(
            userProperty = UserPropertyAnalyticsModel("favorite_color"),
            value = "blue"
        )
    }
}

@Composable
fun ExampleScreen(
    modifier: Modifier = Modifier,
    onEventClick: () -> Unit,
    onEventOncePerSessionClick: () -> Unit,
    onEventOncePerLifetimeClick: () -> Unit,
    onUserPropertyClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TAAnalytics Example",
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = onEventClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Track Event")
        }

        Button(
            onClick = onEventOncePerSessionClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Track Event Once Per Session")
        }

        Button(
            onClick = onEventOncePerLifetimeClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Track Event Once Per Lifetime")
        }

        Button(
            onClick = onUserPropertyClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Set User Property")
        }

        Text(
            text = "Check Logcat for analytics logs",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExampleScreenPreview() {
    TAAnalyticsTheme {
        ExampleScreen(
            onEventClick = {},
            onEventOncePerSessionClick = {},
            onEventOncePerLifetimeClick = {},
            onUserPropertyClick = {}
        )
    }
}