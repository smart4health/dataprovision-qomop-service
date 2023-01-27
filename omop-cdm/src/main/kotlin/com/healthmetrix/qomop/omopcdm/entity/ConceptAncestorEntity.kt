package com.healthmetrix.qomop.omopcdm.entity

import com.healthmetrix.qomop.omopcdm.api.ConceptAncestor
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable

/**
 * See OMOP CDMv6 documentation on this entity: [https://ohdsi.github.io/CommonDataModel/cdm60.html#CONCEPT_ANCESTOR]
 */
@Entity
@Table(name = "concept_ancestor")
@IdClass(ConceptAncestorId::class)
data class ConceptAncestorEntity(
    @Id
    @Column(name = "ancestor_concept_id")
    val ancestorConceptId: Int,

    @Id
    @Column(name = "descendant_concept_id")
    val descendantConceptId: Int,

    @Column(name = "min_levels_of_separation")
    val minLevelsOfSeparation: Int,

    @Column(name = "max_levels_of_separation")
    val maxLevelsOfSeparation: Int,
)

fun ConceptAncestorEntity.toConceptAncestor() = ConceptAncestor(
    ancestorConceptId = ancestorConceptId,
    descendantConceptId = descendantConceptId,
    minLevelsOfSeparation = minLevelsOfSeparation,
    maxLevelsOfSeparation = maxLevelsOfSeparation,
)

/**
 * PURELY FOR HIBERNATE please do not use elsewhere
 */
data class ConceptAncestorId(
    val ancestorConceptId: ConceptEntity? = null,
    val descendantConceptId: ConceptEntity? = null,
) : Serializable
