package com.healthmetrix.qomop.omopcdm.entity

import com.healthmetrix.qomop.omopcdm.api.Relationship
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * See OMOP CDMv6 documentation on this entity: [https://ohdsi.github.io/CommonDataModel/cdm60.html#RELATIONSHIP]
 */
@Entity
@Table(name = "relationship")
data class RelationshipEntity(
    @Id
    @Column(name = "relationship_id")
    val id: String,

    @Column(name = "relationship_name")
    val name: String,

    @Column(name = "is_hierarchical")
    val isHierarchical: String,

    @Column(name = "defines_ancestry")
    val definesAncestry: String,

    @Column(name = "reverse_relationship_id")
    val reverseId: String,

    @Column(name = "relationship_concept_id")
    val conceptId: Int,
)

fun RelationshipEntity.toRelationship() = Relationship(
    id = id,
    name = name,
    isHierarchical = isHierarchical,
    definesAncestry = definesAncestry,
    reverseId = reverseId,
    conceptId = conceptId,
)
