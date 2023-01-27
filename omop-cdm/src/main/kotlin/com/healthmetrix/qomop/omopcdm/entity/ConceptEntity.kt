package com.healthmetrix.qomop.omopcdm.entity

import com.healthmetrix.qomop.omopcdm.api.Concept
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * See OMOP CDMv6 documentation on this entity: [https://ohdsi.github.io/CommonDataModel/cdm60.html#CONCEPT]
 */
@Entity
@Table(name = "concept")
data class ConceptEntity(
    @Id
    @Column(name = "concept_id")
    val id: Int,

    @Column(name = "concept_name")
    val name: String,

    @Column(name = "domain_id")
    val domainId: String?,

    @ManyToOne(optional = false, targetEntity = VocabularyEntity::class, fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", referencedColumnName = "vocabulary_id")
    val vocabulary: VocabularyEntity,

    @Column(name = "concept_class_id")
    val conceptClassId: String,

    @Column(name = "standard_concept")
    val standardConcept: String?,

    @Column(name = "concept_code")
    val conceptCode: String,

    @Column(name = "valid_start_date")
    val validStart: LocalDate,

    @Column(name = "valid_end_date")
    val validEnd: LocalDate,

    @Column(name = "invalid_reason")
    val invalidReason: String?,
)

fun ConceptEntity.toConcept() = Concept(
    id = id,
    name = name,
    domainId = domainId,
    vocabulary = vocabulary.toVocabulary(),
    conceptClassId = conceptClassId,
    standardConcept = "S" == standardConcept,
    conceptCode = conceptCode,
    validStart = validStart,
    validEnd = validEnd,
    invalidReason = invalidReason,
)
