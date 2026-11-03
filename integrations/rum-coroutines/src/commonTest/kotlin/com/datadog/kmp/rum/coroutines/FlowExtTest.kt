/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.coroutines

import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumMonitor
import com.datadog.tools.random.randomException
import com.datadog.tools.random.randomInt
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlowExtTest {

    private val mockRumMonitor: RumMonitor = mock()

    @Test
    fun `M add RUM Error W flow emits error`() {
        // Given
        val fakeException = randomException()
        val flow = flow<String> { throw fakeException }

        // When
        assertFailsWith(fakeException::class) {
            runBlocking {
                flow.sendErrorToDatadog { mockRumMonitor }.collect()
            }
        }

        // Then
        verify {
            mockRumMonitor.addError(ERROR_FLOW, RumErrorSource.SOURCE, fakeException, emptyMap())
        }
        verifyNoMoreCalls(mockRumMonitor)
    }

    @Test
    fun `M not add RUM Error W flow emits successfully`() {
        // Given
        val fakeData = List(randomInt(from = 1, until = 10)) { randomInt() }

        // When
        val result = runBlocking {
            fakeData.asFlow().sendErrorToDatadog { mockRumMonitor }.toList()
        }

        // Then
        assertEquals(fakeData, result)
        verifyNoMoreCalls(mockRumMonitor)
    }
}
