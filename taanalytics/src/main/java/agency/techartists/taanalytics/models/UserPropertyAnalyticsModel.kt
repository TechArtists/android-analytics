//  UserPropertyAnalyticsModel.kt
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
 * Type-safe wrapper for analytics user property names.
 *
 * Best practice: At most 24 alphanumeric characters or underscores.
 * Usually in snake_case, but consult with your BI team for their preference.
 *
 * @param rawValue The user property name
 * @param isInternalUserProperty Whether this property is tracked internally by TAAnalytics library
 */
data class UserPropertyAnalyticsModel(
    val rawValue: String,
    internal val isInternalUserProperty: Boolean = false
) {
    /**
     * Creates a new user property with a prefix added to the name.
     */
    fun userPropertyByPrefixing(prefix: String): UserPropertyAnalyticsModel {
        return UserPropertyAnalyticsModel(prefix + rawValue, isInternalUserProperty)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserPropertyAnalyticsModel) return false
        return rawValue == other.rawValue
    }

    override fun hashCode(): Int {
        return rawValue.hashCode()
    }
}

/**
 * Trimmed version of UserPropertyAnalyticsModel after platform-specific length restrictions are applied.
 */
data class UserPropertyAnalyticsModelTrimmed(
    val rawValue: String
)
