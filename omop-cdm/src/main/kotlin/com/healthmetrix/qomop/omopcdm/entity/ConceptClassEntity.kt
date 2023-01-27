package com.healthmetrix.qomop.omopcdm.entity

import com.healthmetrix.qomop.omopcdm.api.ConceptClass
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * See OMOP CDMv6 documentation on this entity: [https://ohdsi.github.io/CommonDataModel/cdm60.html#CONCEPT_CLASS]
 */
@Entity
@Table(name = "concept_class")
data class ConceptClassEntity(

    @Id
    @Column(name = "concept_class_id")
    val id: String,

    @Column(name = "concept_class_name")
    val name: String,

    @Column(name = "concept_class_concept_id")
    val conceptId: Int,
)

fun ConceptClassEntity.toConceptClass() = ConceptClass(
    id = id,
    name = name,
    conceptId = conceptId,
)
