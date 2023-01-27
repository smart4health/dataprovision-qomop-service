package com.healthmetrix.qomop.tracer.usecases

import com.github.michaelbull.result.Result
import com.healthmetrix.qomop.tracer.controllers.QueryBody

interface ConceptTracer {
    fun contains(queryBody: QueryBody): Result<Boolean, TracingError>
}

data class TracingQuery(
    val queryConceptId: Int,
    val targetConceptId: Int,
    val relationshipId: String,
)

sealed class TracingError {
    object ConceptNotFound : TracingError()
}
