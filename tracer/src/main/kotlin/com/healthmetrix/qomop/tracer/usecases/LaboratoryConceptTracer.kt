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
class LaboratoryConceptTracer(
    private val traceConceptByRelationshipUseCase: TraceConceptByRelationshipUseCase,
    private val conceptRepository: ConceptRepository,
) : ConceptTracer {
    val bloodGroupLoincConceptId = 3002529 // system = LOINC, code: "883-9"

    override fun contains(queryBody: QueryBody): Result<Boolean, TracingError> = binding {
        val concept = conceptRepository
            .findByConceptCode(queryBody.code)
            .firstOrNull { queryBody.system == it.vocabulary.id.asOmopVocabularyId()?.fhirSystemUri }
            .toResultOr { TracingError.ConceptNotFound }
            .bind()

        traceConceptByRelationshipUseCase(
            TracingQuery(
                queryConceptId = concept.id,
                targetConceptId = bloodGroupLoincConceptId,
                relationshipId = Relationship.CONTAINED_IN_PANEL.id,
            ),
        )
    }
}
