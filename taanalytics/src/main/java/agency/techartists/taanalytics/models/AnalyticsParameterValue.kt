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

package agency.techartists.taanalytics.models

/**
 * Base interface for parameter values that can be sent to analytics platforms.
 * Most analytics platforms (Firebase, Amplitude, etc.) only accept String and numeric types.
 */
interface AnalyticsBaseParameterValue {
    fun toAnalyticsValue(): Any
    override fun toString(): String
}

// Extension to convert Kotlin types to analytics values
fun String.toAnalyticsValue(): AnalyticsBaseParameterValue =
    object : AnalyticsBaseParameterValue {
        override fun toAnalyticsValue(): Any = this@toAnalyticsValue
        override fun toString(): String = this@toAnalyticsValue
    }

fun Int.toAnalyticsValue(): AnalyticsBaseParameterValue =
    object : AnalyticsBaseParameterValue {
        override fun toAnalyticsValue(): Any = this@toAnalyticsValue
        override fun toString(): String = this@toAnalyticsValue.toString()
    }

fun Long.toAnalyticsValue(): AnalyticsBaseParameterValue =
    object : AnalyticsBaseParameterValue {
        override fun toAnalyticsValue(): Any = this@toAnalyticsValue
        override fun toString(): String = this@toAnalyticsValue.toString()
    }

fun Double.toAnalyticsValue(): AnalyticsBaseParameterValue =
    object : AnalyticsBaseParameterValue {
        override fun toAnalyticsValue(): Any = this@toAnalyticsValue
        override fun toString(): String = this@toAnalyticsValue.toString()
    }

fun Float.toAnalyticsValue(): AnalyticsBaseParameterValue =
    object : AnalyticsBaseParameterValue {
        override fun toAnalyticsValue(): Any = this@toAnalyticsValue
        override fun toString(): String = this@toAnalyticsValue.toString()
    }

fun Boolean.toAnalyticsValue(): AnalyticsBaseParameterValue =
    object : AnalyticsBaseParameterValue {
        override fun toAnalyticsValue(): Any = this@toAnalyticsValue
        override fun toString(): String = this@toAnalyticsValue.toString()
    }
