package tech.testra.reportal.api.rest.extensions

import tech.testra.reportal.api.rest.response.ErrorResponse

fun Exception.toErrorResponse(): ErrorResponse =
    ErrorResponse(error = this::class.java.simpleName, msg = this.message)

fun Throwable.toErrorResponse(): ErrorResponse =
    ErrorResponse(error = this::class.java.simpleName, msg = this.message)
