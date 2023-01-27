package com.healthmetrix.qomop.omopcdm.repository

import com.healthmetrix.qomop.omopcdm.api.Relationship
import com.healthmetrix.qomop.omopcdm.api.RelationshipRepository
import com.healthmetrix.qomop.omopcdm.entity.RelationshipEntity
import com.healthmetrix.qomop.omopcdm.entity.toRelationship
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
class RemoteRelationshipRepository(
    private val relationshipRepository: RelationshipCrudRepository,
) : RelationshipRepository {

    override fun findById(relationshipId: String): Relationship? =
        relationshipRepository.findById(relationshipId).orElse(null)?.toRelationship()
}

interface RelationshipCrudRepository : CrudRepository<RelationshipEntity, String>
