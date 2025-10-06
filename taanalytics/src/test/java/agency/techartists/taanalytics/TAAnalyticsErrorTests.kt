//  TAAnalyticsErrorTests.kt
//
//  Copyright (c) 2024 Tech Artists Agency SRL

package agency.techartists.taanalytics

import agency.techartists.taanalytics.core.TAPermissionType
import agency.techartists.taanalytics.mock.MockTAAnalytics
import agency.techartists.taanalytics.models.toAnalyticsValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TAAnalyticsErrorTests {

    private lateinit var mockAnalytics: MockTAAnalytics

    @Before
    fun setup() {
        mockAnalytics = MockTAAnalytics()
    }

    @Test
    fun `trackErrorEvent sends error event`() {
        mockAnalytics.trackErrorEvent("network_timeout")

        val errorEvents = mockAnalytics.findEvents("error")
        assertEquals(1, errorEvents.size)
    }

    @Test
    fun `trackErrorEvent with exception`() {
        val exception = IllegalStateException("Test exception")

        mockAnalytics.trackErrorEvent("operation_failed", exception)

        val errorEvents = mockAnalytics.findEvents("error")
        assertEquals(1, errorEvents.size)
    }

    @Test
    fun `trackErrorEvent with extra params`() {
        val extraParams = mapOf(
            "endpoint" to "/api/users".toAnalyticsValue(),
            "retry_count" to 3.toAnalyticsValue()
        )

        mockAnalytics.trackErrorEvent("api_error", extraParams = extraParams)

        val errorEvents = mockAnalytics.findEvents("error")
        assertEquals(1, errorEvents.size)
    }

    @Test
    fun `trackErrorCorrected sends error_corrected event`() {
        mockAnalytics.trackErrorCorrected("network_timeout")

        val correctedEvents = mockAnalytics.findEvents("error_corrected")
        assertEquals(1, correctedEvents.size)
    }

    @Test
    fun `error and correction flow`() {
        // Track error
        mockAnalytics.trackErrorEvent("payment_processing")

        // Later, track correction
        mockAnalytics.trackErrorCorrected("payment_processing")

        assertEquals(1, mockAnalytics.findEvents("error").size)
        assertEquals(1, mockAnalytics.findEvents("error_corrected").size)
    }

    @Test
    fun `trackPermissionScreenShow with standard type`() {
        mockAnalytics.trackPermissionScreenShow(TAPermissionType.PUSH_NOTIFICATIONS)

        val viewEvents = mockAnalytics.findEvents("ui_view_show")
        assertEquals(1, viewEvents.size)
        assertEquals("permission", mockAnalytics.lastViewShow?.name)
        assertEquals("push_notifications", mockAnalytics.lastViewShow?.type)
    }

    @Test
    fun `trackPermissionScreenShow with custom type`() {
        mockAnalytics.trackPermissionScreenShow("bluetooth")

        val viewEvents = mockAnalytics.findEvents("ui_view_show")
        assertEquals(1, viewEvents.size)
        assertEquals("permission", mockAnalytics.lastViewShow?.name)
        assertEquals("bluetooth", mockAnalytics.lastViewShow?.type)
    }

    @Test
    fun `trackPermissionButtonTap with allowed`() {
        mockAnalytics.trackPermissionButtonTap(allowed = true, TAPermissionType.LOCATION)

        val buttonEvents = mockAnalytics.findEvents("ui_button_tap")
        assertEquals(1, buttonEvents.size)
    }

    @Test
    fun `trackPermissionButtonTap with denied`() {
        mockAnalytics.trackPermissionButtonTap(allowed = false, TAPermissionType.CAMERA)

        val buttonEvents = mockAnalytics.findEvents("ui_button_tap")
        assertEquals(1, buttonEvents.size)
    }

    @Test
    fun `trackPermissionButtonTap with custom status`() {
        mockAnalytics.trackPermissionButtonTap("already_granted", TAPermissionType.MICROPHONE)

        val buttonEvents = mockAnalytics.findEvents("ui_button_tap")
        assertEquals(1, buttonEvents.size)
    }

    @Test
    fun `permission flow tracking`() {
        // Show permission screen
        mockAnalytics.trackPermissionScreenShow(TAPermissionType.PUSH_NOTIFICATIONS)

        // User allows
        mockAnalytics.trackPermissionButtonTap(allowed = true, TAPermissionType.PUSH_NOTIFICATIONS)

        assertEquals(1, mockAnalytics.findEvents("ui_view_show").size)
        assertEquals(1, mockAnalytics.findEvents("ui_button_tap").size)
    }

    @Test
    fun `all permission types are available`() {
        val types = listOf(
            TAPermissionType.PUSH_NOTIFICATIONS,
            TAPermissionType.LOCATION,
            TAPermissionType.CAMERA,
            TAPermissionType.MICROPHONE,
            TAPermissionType.STORAGE,
            TAPermissionType.CONTACTS,
            TAPermissionType.CALENDAR,
            TAPermissionType.PHONE,
            TAPermissionType.SMS
        )

        types.forEach { type ->
            assertNotNull(type.value)
            assertTrue(type.value.isNotEmpty())
        }
    }
}
