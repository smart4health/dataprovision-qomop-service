package com.healthmetrix.qomop.omopcdm.entity

import com.healthmetrix.qomop.omopcdm.api.Vocabulary
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * See OMOP CDMv6 documentation on this entity: [https://ohdsi.github.io/CommonDataModel/cdm60.html#VOCABULARY]
 */
@Entity
@Table(name = "vocabulary")
data class VocabularyEntity(
    @Id
    @Column(name = "vocabulary_id")
    val id: String,

    @Column(name = "vocabulary_name")
    val name: String,

    @Column(name = "vocabulary_reference")
    val reference: String?,

    @Column(name = "vocabulary_version")
    val version: String?,

    @Column(name = "vocabulary_concept_id")
    val conceptId: Int,
)

fun VocabularyEntity.toVocabulary() = Vocabulary(
    id = id,
    name = name,
    reference = reference,
    version = version,
    conceptId = conceptId,
)
