package com.healthmetrix.qomop.omopcdm.api

import java.time.LocalDate

data class ConceptRelationship(
    val sourceConcept: Concept,
    val targetConcept: Concept,
    val relationship: Relationship,
    val validStart: LocalDate,
    val validEnd: LocalDate,
    val invalidReason: String?,
)

interface ConceptRelationshipRepository {
    fun findBySourceConceptId(conceptId: Int): List<ConceptRelationship>
    fun findByTargetConceptId(conceptId: Int): List<ConceptRelationship>
}
