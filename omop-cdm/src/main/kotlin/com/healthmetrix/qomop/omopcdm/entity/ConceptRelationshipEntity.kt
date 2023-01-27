package com.healthmetrix.qomop.omopcdm.entity

import com.healthmetrix.qomop.omopcdm.api.ConceptRelationship
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDate

/**
 * See OMOP CDMv6 documentation on this entity: [https://ohdsi.github.io/CommonDataModel/cdm60.html#CONCEPT_RELATIONSHIP]
 */
@Entity
@IdClass(ConceptRelationshipId::class)
@Table(name = "concept_relationship")
data class ConceptRelationshipEntity(
    @Id
    @ManyToOne(optional = false, targetEntity = ConceptEntity::class, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "concept_id_1",
        referencedColumnName = "concept_id",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT),
    )
    val sourceConcept: ConceptEntity,

    @Id
    @ManyToOne(optional = false, targetEntity = ConceptEntity::class, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "concept_id_2",
        referencedColumnName = "concept_id",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT),
    )
    val targetConcept: ConceptEntity,

    @Id
    @ManyToOne(optional = false, targetEntity = RelationshipEntity::class, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "relationship_id",
        referencedColumnName = "relationship_id",
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT),
    )
    val relationship: RelationshipEntity,

    @Column(name = "valid_start_date")
    val validStart: LocalDate,

    @Column(name = "valid_end_date")
    val validEnd: LocalDate,

    @Column(name = "invalid_reason")
    val invalidReason: String?,
)

fun ConceptRelationshipEntity.toConceptRelationship() = ConceptRelationship(
    sourceConcept = sourceConcept.toConcept(),
    targetConcept = targetConcept.toConcept(),
    relationship = relationship.toRelationship(),
    validStart = validStart,
    validEnd = validEnd,
    invalidReason = invalidReason,
)

/**
 * PURELY FOR HIBERNATE please do not use elsewhere
 */
data class ConceptRelationshipId(
    val sourceConcept: ConceptEntity? = null,
    val targetConcept: ConceptEntity? = null,
    val relationship: RelationshipEntity? = null,
) : Serializable
