package com.healthmetrix.qomop.tracer.usecases

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.toResultOr
import com.healthmetrix.qomop.commons.Relationship
import com.healthmetrix.qomop.commons.asOmopVocabularyId
import com.healthmetrix.qomop.omopcdm.api.ConceptRepository
import com.healthmetrix.qomop.tracer.controllers.QueryBody
import org.springframework.stereotype.Component

@Component
class SurgeriesConceptTracer(
    private val traceConceptByRelationshipUseCase: TraceConceptByRelationshipUseCase,
    private val conceptRepository: ConceptRepository,
) : ConceptTracer {
    val surgicalProcedureSnomedConceptId = 4301351 // system SNOMED, code 387713003

    override fun contains(queryBody: QueryBody): Result<Boolean, TracingError> = binding {
        val concept = conceptRepository
            .findByConceptCode(queryBody.code)
            .firstOrNull { queryBody.system == it.vocabulary.id.asOmopVocabularyId()?.fhirSystemUri }
            .toResultOr { TracingError.ConceptNotFound }
            .bind()

        traceConceptByRelationshipUseCase(
            TracingQuery(
                queryConceptId = concept.id,
                targetConceptId = surgicalProcedureSnomedConceptId,
                relationshipId = Relationship.IS_A.id,
            ),
        )
    }
}
