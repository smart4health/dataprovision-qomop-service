package com.healthmetrix.qomop.omopcdm.api

data class ConceptAncestor(
    val ancestorConceptId: Int,
    val descendantConceptId: Int,
    val minLevelsOfSeparation: Int,
    val maxLevelsOfSeparation: Int,
)

interface ConceptAncestorRepository {
    fun findByAncestorConceptId(conceptAncestorId: Int): List<ConceptAncestor>
}
