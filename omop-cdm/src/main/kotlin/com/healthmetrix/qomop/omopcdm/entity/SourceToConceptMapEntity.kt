package com.healthmetrix.qomop.omopcdm.entity

import com.healthmetrix.qomop.omopcdm.api.SourceToConceptMap
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDate

/**
 * See OMOP CDMv6 documentation on this entity: [https://ohdsi.github.io/CommonDataModel/cdm60.html#SOURCE_TO_CONCEPT_MAP]
 */
@Entity
@Table(name = "source_to_concept_map")
@IdClass(SourceToConceptMapId::class)
data class SourceToConceptMapEntity(
    @Id
    @Column(name = "source_code")
    val sourceCode: String,

    @Id
    @Column(name = "source_vocabulary_id")
    val sourceVocabularyId: String,

    @Id
    @Column(name = "target_concept_id")
    val targetConceptId: Int,

    @Column(name = "source_code_description")
    val sourceCodeDescription: String?,

    @Column(name = "source_concept_id")
    val sourceConceptId: Int = 0,

    @Column(name = "target_vocabulary_id")
    val targetVocabularyId: String,

    @Column(name = "valid_start_date")
    val validStart: LocalDate,

    @Id
    @Column(name = "valid_end_date")
    val validEnd: LocalDate,

    @Column(name = "invalid_reason")
    val invalidReason: String?,
)

fun SourceToConceptMapEntity.toSourceToConceptMap() = SourceToConceptMap(
    sourceCode = sourceCode,
    sourceConceptId = sourceConceptId,
    sourceVocabularyId = sourceVocabularyId,
    sourceCodeDescription = sourceCodeDescription,
    targetConceptId = targetConceptId,
    targetVocabularyId = targetVocabularyId,
    validStart = validStart,
    validEnd = validEnd,
    invalidReason = invalidReason,
)

/**
 * PURELY FOR HIBERNATE please do not use elsewhere
 */
data class SourceToConceptMapId(
    val sourceCode: String? = null,
    val sourceVocabularyId: String? = null,
    val targetConceptId: Int? = null,
    val validEnd: LocalDate? = null,
) : Serializable
