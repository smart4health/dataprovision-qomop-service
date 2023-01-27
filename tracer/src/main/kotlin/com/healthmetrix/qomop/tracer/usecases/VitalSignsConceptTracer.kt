package com.healthmetrix.qomop.tracer.usecases

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.healthmetrix.qomop.commons.SupportedVocabulary
import com.healthmetrix.qomop.tracer.controllers.QueryBody
import org.springframework.stereotype.Component

@Component
class VitalSignsConceptTracer : ConceptTracer {
    val vitalSignConceptCodes = setOf(
        "85353-1",
        "9279-1",
        "8867-4",
        "2708-6",
        "8310-5",
        "8302-2",
        "9843-4",
        "29463-7",
        "39156-5",
        "85354-9",
        "8480-6",
        "8462-4",
    )

    override fun contains(queryBody: QueryBody): Result<Boolean, TracingError> =
        Ok(queryBody.system.equals(SupportedVocabulary.LOINC.fhirSystemUri) && vitalSignConceptCodes.contains(queryBody.code))
}
