/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.coroutines

import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal const val ERROR_FLOW: String = "Coroutine Flow error"

/**
 *  Returns a [Flow] that will send a RUM Error event if this [Flow] emits an error.
 *  Note that the error will also be emitted by the returned [Flow].
 *
 *  @param T the type of data in the [Flow].
 *
 *  @return the new [Flow] instance.
 */
fun <T> Flow<T>.sendErrorToDatadog(): Flow<T> = sendErrorToDatadog { RumMonitor.get() }

@Suppress("TooGenericExceptionCaught", "SuspendFunSwallowedCancellation")
internal fun <T> Flow<T>.sendErrorToDatadog(rumMonitorProvider: () -> RumMonitor): Flow<T> {
    return flow {
        try {
            collect { value -> emit(value) }
        } catch (e: Throwable) {
            rumMonitorProvider()
                .addError(
                    ERROR_FLOW,
                    RumErrorSource.SOURCE,
                    e
                )
            throw e
        }
    }
}
