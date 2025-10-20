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
import agency.techartists.taanalytics.core.TAAnalytics
import agency.techartists.taanalytics.core.TAAnalyticsConfig
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.ViewAnalyticsModel
import android.app.Application
import android.content.res.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Custom events for the analytics example app
 */
object CustomEvents {
    val CALL_PLACED = EventAnalyticsModel("call_placed")
    val DID_RECEIVE_MEMORY_WARNING = EventAnalyticsModel("did_receive_memory_warning")
}

/**
 * Custom user properties for the analytics example app
 */
object CustomUserProperties {
    val INSTALL_ORIENTATION = UserPropertyAnalyticsModel("install_orientation")
}

/**
 * Custom view models for contacts screens
 */
object CustomViews {
    val CONTACTS_PERMISSION_DENIED = ViewAnalyticsModel(name = "contacts", type = "permission denied")
    val CONTACTS_PERMISSION_NOT_DETERMINED = ViewAnalyticsModel(name = "contacts", type = "permission not determined")
    val CONTACTS_WITH_PERMISSION = ViewAnalyticsModel(name = "contacts") // or type: "with permission"

    /** The type will be set at runtime as the ID of the contact */
    val CONTACT = ViewAnalyticsModel("contact")
}

class AnalyticsExampleApp : Application() {

    lateinit var analytics: TAAnalytics
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize TAAnalytics
        val config = TAAnalyticsConfig(
            analyticsVersion = "1.0",
            adaptors = listOf(
                LogcatAnalyticsAdaptor()
                // Add more adaptors here:
                // FirebaseAnalyticsAdaptor(firebaseAnalytics)
            ),
            sharedPreferences = getSharedPreferences("TAAnalyticsExample", MODE_PRIVATE)
        )

        analytics = TAAnalytics(applicationContext, config)

        // Start analytics
        applicationScope.launch {
            analytics.start()

            // Set custom install property: device orientation at install time
            // (can be set immediately after start)
            val orientation = when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> "landscape"
                Configuration.ORIENTATION_PORTRAIT -> "portrait"
                else -> "undefined"
            }
            analytics.set(
                userProperty = CustomUserProperties.INSTALL_ORIENTATION,
                value = orientation
            )
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        analytics.track(event = CustomEvents.DID_RECEIVE_MEMORY_WARNING)
    }
}
