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

import agency.techartists.taanalytics.mock.MockTAAnalytics
import agency.techartists.taanalytics.models.EventAnalyticsModel
import agency.techartists.taanalytics.models.UserPropertyAnalyticsModel
import agency.techartists.taanalytics.models.toAnalyticsValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TAAnalyticsBasicTests {

    private lateinit var mockAnalytics: MockTAAnalytics

    @Before
    fun setup() {
        mockAnalytics = MockTAAnalytics()
    }

    @Test
    fun `track event without parameters`() {
        mockAnalytics.track(EventAnalyticsModel("test_event"))

        assertEquals(1, mockAnalytics.eventsSent.size)
        val (event, params) = mockAnalytics.eventsSent[0]
        assertEquals("test_event", event.rawValue)
        assertNull(params)
    }

    @Test
    fun `track event with parameters`() {
        val params = mapOf(
            "key1" to "value1".toAnalyticsValue(),
            "key2" to 42.toAnalyticsValue()
        )

        mockAnalytics.track(EventAnalyticsModel("test_event"), params)

        assertEquals(1, mockAnalytics.eventsSent.size)
        val (event, sentParams) = mockAnalytics.eventsSent[0]
        assertEquals("test_event", event.rawValue)
        assertEquals(2, sentParams?.size)
    }

    @Test
    fun `set user property`() {
        val property = UserPropertyAnalyticsModel("user_tier")
        mockAnalytics.set(property, "premium")

        assertEquals("premium", mockAnalytics.userPropertiesSet[property])
        assertEquals("premium", mockAnalytics.get(property))
    }

    @Test
    fun `get non-existent user property returns null`() {
        val property = UserPropertyAnalyticsModel("non_existent")
        assertNull(mockAnalytics.get(property))
    }

    @Test
    fun `set user property to null removes it`() {
        val property = UserPropertyAnalyticsModel("user_tier")
        mockAnalytics.set(property, "premium")
        assertEquals("premium", mockAnalytics.get(property))

        mockAnalytics.set(property, null)
        assertNull(mockAnalytics.get(property))
    }

    @Test
    fun `track multiple events`() {
        mockAnalytics.track(EventAnalyticsModel("event1"))
        mockAnalytics.track(EventAnalyticsModel("event2"))
        mockAnalytics.track(EventAnalyticsModel("event3"))

        assertEquals(3, mockAnalytics.eventsSent.size)
        assertEquals("event1", mockAnalytics.eventsSent[0].first.rawValue)
        assertEquals("event2", mockAnalytics.eventsSent[1].first.rawValue)
        assertEquals("event3", mockAnalytics.eventsSent[2].first.rawValue)
    }

    @Test
    fun `findEvents returns matching events`() {
        mockAnalytics.track(EventAnalyticsModel("login"))
        mockAnalytics.track(EventAnalyticsModel("purchase"))
        mockAnalytics.track(EventAnalyticsModel("login"))

        val loginEvents = mockAnalytics.findEvents("login")
        assertEquals(2, loginEvents.size)

        val purchaseEvents = mockAnalytics.findEvents("purchase")
        assertEquals(1, purchaseEvents.size)
    }

    @Test
    fun `findLastEvent returns most recent matching event`() {
        mockAnalytics.track(EventAnalyticsModel("login"), mapOf("attempt" to 1.toAnalyticsValue()))
        mockAnalytics.track(EventAnalyticsModel("purchase"))
        mockAnalytics.track(EventAnalyticsModel("login"), mapOf("attempt" to 2.toAnalyticsValue()))

        val lastLogin = mockAnalytics.findLastEvent("login")
        assertNotNull(lastLogin)
        assertEquals("login", lastLogin?.first?.rawValue)
    }

    @Test
    fun `clear removes all recorded data`() {
        mockAnalytics.track(EventAnalyticsModel("test"))
        mockAnalytics.set(UserPropertyAnalyticsModel("prop"), "value")
        mockAnalytics.userID = "user_123"

        assertEquals(1, mockAnalytics.eventsSent.size)
        assertEquals(1, mockAnalytics.userPropertiesSet.size)
        assertEquals("user_123", mockAnalytics.userID)

        mockAnalytics.clear()

        assertEquals(0, mockAnalytics.eventsSent.size)
        assertEquals(0, mockAnalytics.userPropertiesSet.size)
        assertNull(mockAnalytics.userID)
    }

    @Test
    fun `userID can be set and retrieved`() {
        assertNull(mockAnalytics.userID)

        mockAnalytics.userID = "user_123"
        assertEquals("user_123", mockAnalytics.userID)

        mockAnalytics.userID = null
        assertNull(mockAnalytics.userID)
    }

    @Test
    fun `userPseudoID is available`() {
        assertNotNull(mockAnalytics.userPseudoID)
        assertTrue(mockAnalytics.userPseudoID!!.isNotEmpty())
    }

    @Test
    fun `different parameter types are supported`() {
        val params = mapOf(
            "string" to "value".toAnalyticsValue(),
            "int" to 42.toAnalyticsValue(),
            "long" to 123L.toAnalyticsValue(),
            "double" to 3.14.toAnalyticsValue(),
            "float" to 2.5f.toAnalyticsValue(),
            "boolean" to true.toAnalyticsValue()
        )

        mockAnalytics.track(EventAnalyticsModel("test_types"), params)

        val (_, sentParams) = mockAnalytics.eventsSent[0]
        assertEquals(6, sentParams?.size)
    }
}
