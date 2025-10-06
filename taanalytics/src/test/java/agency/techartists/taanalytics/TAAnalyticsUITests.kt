//  TAAnalyticsUITests.kt
//
//  Copyright (c) 2024 Tech Artists Agency SRL

package agency.techartists.taanalytics

import agency.techartists.taanalytics.mock.MockTAAnalytics
import agency.techartists.taanalytics.models.AnalyticsViewFunnelStepDetails
import agency.techartists.taanalytics.models.SecondaryViewAnalyticsModel
import agency.techartists.taanalytics.models.ViewAnalyticsModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TAAnalyticsUITests {

    private lateinit var mockAnalytics: MockTAAnalytics

    @Before
    fun setup() {
        mockAnalytics = MockTAAnalytics()
    }

    @Test
    fun `track view show updates lastViewShow`() {
        val view = ViewAnalyticsModel("home", type = "main")

        mockAnalytics.track(view)

        assertNotNull(mockAnalytics.lastViewShow)
        assertEquals("home", mockAnalytics.lastViewShow?.name)
        assertEquals("main", mockAnalytics.lastViewShow?.type)
    }

    @Test
    fun `track view show sends ui_view_show event`() {
        val view = ViewAnalyticsModel("settings")

        mockAnalytics.track(view)

        val viewEvents = mockAnalytics.findEvents("ui_view_show")
        assertEquals(1, viewEvents.size)
    }

    @Test
    fun `track secondary view`() {
        val mainView = ViewAnalyticsModel("home")
        val secondaryView = SecondaryViewAnalyticsModel("popup", "notification", mainView)

        mockAnalytics.track(secondaryView)

        val viewEvents = mockAnalytics.findEvents("ui_view_show")
        assertEquals(1, viewEvents.size)
    }

    @Test
    fun `track button tap sends ui_button_tap event`() {
        val view = ViewAnalyticsModel("home")

        mockAnalytics.trackButtonTap("subscribe", view)

        val buttonEvents = mockAnalytics.findEvents("ui_button_tap")
        assertEquals(1, buttonEvents.size)
    }

    @Test
    fun `track button tap with extra parameter`() {
        val view = ViewAnalyticsModel("paywall")

        mockAnalytics.trackButtonTap("purchase", view, extra = "premium_plan")

        val buttonEvents = mockAnalytics.findEvents("ui_button_tap")
        assertEquals(1, buttonEvents.size)
    }

    @Test
    fun `track button tap with index`() {
        val view = ViewAnalyticsModel("list")

        mockAnalytics.trackButtonTap("select_item", view, index = 2)

        val buttonEvents = mockAnalytics.findEvents("ui_button_tap")
        assertEquals(1, buttonEvents.size)
    }

    @Test
    fun `track view with funnel step`() {
        val view = ViewAnalyticsModel(
            name = "onboarding_step_1",
            type = "onboarding",
            funnelStep = AnalyticsViewFunnelStepDetails(
                funnelName = "onboarding",
                step = 1,
                isOptionalStep = false,
                isFinalStep = false
            )
        )

        mockAnalytics.track(view)

        assertNotNull(mockAnalytics.lastViewShow)
        assertEquals("onboarding_step_1", mockAnalytics.lastViewShow?.name)
        assertNotNull(mockAnalytics.lastViewShow?.funnelStep)
        assertEquals("onboarding", mockAnalytics.lastViewShow?.funnelStep?.funnelName)
        assertEquals(1, mockAnalytics.lastViewShow?.funnelStep?.step)
    }

    @Test
    fun `track multiple views updates lastViewShow to most recent`() {
        val view1 = ViewAnalyticsModel("home")
        val view2 = ViewAnalyticsModel("settings")
        val view3 = ViewAnalyticsModel("profile")

        mockAnalytics.track(view1)
        mockAnalytics.track(view2)
        mockAnalytics.track(view3)

        assertEquals("profile", mockAnalytics.lastViewShow?.name)
        assertEquals(3, mockAnalytics.findEvents("ui_view_show").size)
    }

    @Test
    fun `ViewAnalyticsModel withType creates copy with new type`() {
        val view = ViewAnalyticsModel("home", type = "main")
        val modifiedView = view.withType("secondary")

        assertEquals("home", modifiedView.name)
        assertEquals("secondary", modifiedView.type)
        assertEquals("main", view.type) // Original unchanged
    }

    @Test
    fun `ViewAnalyticsModel withFunnelStep creates copy with funnel`() {
        val view = ViewAnalyticsModel("onboarding")
        val funnel = AnalyticsViewFunnelStepDetails(
            funnelName = "user_onboarding",
            step = 1,
            isOptionalStep = false,
            isFinalStep = false
        )
        val modifiedView = view.withFunnelStep(funnel)

        assertEquals("onboarding", modifiedView.name)
        assertNotNull(modifiedView.funnelStep)
        assertEquals("user_onboarding", modifiedView.funnelStep?.funnelName)
        assertNull(view.funnelStep) // Original unchanged
    }
}
