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

package agency.techartists.taanalytics.data

import androidx.compose.ui.graphics.Color

/**
 * Represents a contact from the device's contacts database
 */
data class Contact(
    val id: String,
    val displayName: String,
    val givenName: String?,
    val familyName: String?,
    val phoneNumber: String?,
    val email: String?,
    val photoUri: String?
) {
    /**
     * Returns initials from the display name (first letter of first two words)
     */
    fun getInitials(): String {
        val parts = displayName.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}"
            parts.size == 1 && parts[0].isNotEmpty() -> parts[0].take(2)
            else -> "?"
        }.uppercase()
    }

    /**
     * Returns a deterministic color based on the contact's name
     */
    fun getAvatarColor(): Color {
        val colors = listOf(
            Color(0xFF1976D2), // Blue
            Color(0xFF388E3C), // Green
            Color(0xFFD32F2F), // Red
            Color(0xFFF57C00), // Orange
            Color(0xFF7B1FA2), // Purple
            Color(0xFF0097A7), // Cyan
            Color(0xFFC2185B), // Pink
            Color(0xFF5D4037), // Brown
        )

        val hash = displayName.hashCode()
        return colors[Math.abs(hash) % colors.size]
    }
}
