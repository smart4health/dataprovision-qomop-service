package com.healthmetrix.qomop.omopcdm.api

import java.time.LocalDate

data class Concept(
    val id: Int,
    val name: String,
    val domainId: String?,
    val vocabulary: Vocabulary,
    val conceptClassId: String,
    val standardConcept: Boolean,
    val conceptCode: String,
    val validStart: LocalDate,
    val validEnd: LocalDate,
    val invalidReason: String?,
)

interface ConceptRepository {
    fun findById(conceptId: Int): Concept?
    fun findByName(conceptName: String): List<Concept>
    fun findByConceptCode(conceptCode: String): List<Concept>
    fun findAncestryRelated(conceptId: Int, distance: Int = 1): List<Concept>
}
