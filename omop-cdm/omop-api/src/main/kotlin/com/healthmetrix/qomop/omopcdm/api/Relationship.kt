package com.healthmetrix.qomop.omopcdm.api

data class Relationship(
    val id: String,
    val name: String,
    val isHierarchical: String,
    val definesAncestry: String,
    val reverseId: String,
    val conceptId: Int,
)

interface RelationshipRepository {
    fun findById(relationshipId: String): Relationship?
}
