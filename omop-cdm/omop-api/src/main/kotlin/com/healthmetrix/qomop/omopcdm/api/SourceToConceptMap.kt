package com.healthmetrix.qomop.omopcdm.api

import java.time.LocalDate

data class SourceToConceptMap(
    val sourceCode: String,
    val sourceConceptId: Int,
    val sourceVocabularyId: String,
    val sourceCodeDescription: String?,
    val targetConceptId: Int,
    val targetVocabularyId: String,
    val validStart: LocalDate,
    val validEnd: LocalDate,
    val invalidReason: String?,
)

interface SourceToConceptMapRepository {
    fun findBySourceConceptCode(sourceConceptCode: String): SourceToConceptMap?
}
