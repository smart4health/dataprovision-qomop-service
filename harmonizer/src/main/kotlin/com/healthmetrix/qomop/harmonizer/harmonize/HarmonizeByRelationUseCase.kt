package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.commons.Relationship
import com.healthmetrix.qomop.omopcdm.api.Concept
import com.healthmetrix.qomop.omopcdm.api.ConceptRelationshipRepository
import org.springframework.stereotype.Component

@Component
class HarmonizeByRelationUseCase(
    private val conceptRelationshipRepository: ConceptRelationshipRepository,
) {
    operator fun invoke(input: Concept): Concept? {
        if (input.isHarmonized()) {
            return input
        }
        val (harmonizedList, standardizedList) = conceptRelationshipRepository.findBySourceConceptId(input.id)
            .filter { it.relationship.id == Relationship.NON_STANDARD_TO_STANDARD_MAPPING.id && it.targetConcept.isStandardized() }
            .map { it.targetConcept }
            .partition { it.isHarmonized() }

        return harmonizedList.firstOrNull() ?: standardizedList.firstOrNull()
    }
}
