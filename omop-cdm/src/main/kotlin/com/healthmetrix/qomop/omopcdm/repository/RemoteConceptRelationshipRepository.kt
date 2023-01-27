package com.healthmetrix.qomop.omopcdm.repository

import com.healthmetrix.qomop.omopcdm.api.ConceptRelationship
import com.healthmetrix.qomop.omopcdm.api.ConceptRelationshipRepository
import com.healthmetrix.qomop.omopcdm.entity.ConceptRelationshipEntity
import com.healthmetrix.qomop.omopcdm.entity.toConceptRelationship
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
class RemoteConceptRelationshipRepository(
    private val conceptRelationshipRepository: ConceptRelationshipCrudRepository,
) : ConceptRelationshipRepository {

    override fun findBySourceConceptId(conceptId: Int): List<ConceptRelationship> =
        conceptRelationshipRepository.findBySourceConceptId(conceptId).map { it.toConceptRelationship() }

    override fun findByTargetConceptId(conceptId: Int): List<ConceptRelationship> =
        conceptRelationshipRepository.findByTargetConceptId(conceptId).map { it.toConceptRelationship() }
}

interface ConceptRelationshipCrudRepository : CrudRepository<ConceptRelationshipEntity, Int> {
    fun findBySourceConceptId(conceptId: Int): List<ConceptRelationshipEntity>
    fun findByTargetConceptId(conceptId: Int): List<ConceptRelationshipEntity>
}
