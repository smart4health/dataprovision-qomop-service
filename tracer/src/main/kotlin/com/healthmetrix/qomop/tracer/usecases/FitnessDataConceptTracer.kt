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
class FitnessDataConceptTracer(
    private val traceConceptByRelationshipUseCase: TraceConceptByRelationshipUseCase,
    private val conceptRepository: ConceptRepository,
) : ConceptTracer {
    val exerciseLoincGroupConceptId = 40652447 // system = LOINC, code: LG41751-5
    val pedometerTrackingPanelLoincConceptId = 40758542 // system = LOINC, code: 55413-9

    override fun contains(queryBody: QueryBody): Result<Boolean, TracingError> = binding {
        val concept = conceptRepository
            .findByConceptCode(queryBody.code)
            .firstOrNull { queryBody.system == it.vocabulary.id.asOmopVocabularyId()?.fhirSystemUri }
            .toResultOr { TracingError.ConceptNotFound }
            .bind()

        traceConceptByRelationshipUseCase(
            TracingQuery(
                queryConceptId = concept.id,
                targetConceptId = exerciseLoincGroupConceptId,
                relationshipId = Relationship.IS_A.id,
            ),
        ) || traceConceptByRelationshipUseCase(
            TracingQuery(
                queryConceptId = concept.id,
                targetConceptId = pedometerTrackingPanelLoincConceptId,
                relationshipId = Relationship.CONTAINED_IN_PANEL.id,
            ),
        )
    }
}
