package com.healthmetrix.qomop.omopcdm.repository

import com.healthmetrix.qomop.omopcdm.api.ConceptAncestor
import com.healthmetrix.qomop.omopcdm.api.ConceptAncestorRepository
import com.healthmetrix.qomop.omopcdm.entity.ConceptAncestorEntity
import com.healthmetrix.qomop.omopcdm.entity.toConceptAncestor
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
class RemoteConceptAncestorRepository(
    private val conceptAncestorRepository: ConceptAncestorCrudRepository,
) : ConceptAncestorRepository {

    override fun findByAncestorConceptId(conceptAncestorId: Int): List<ConceptAncestor> =
        conceptAncestorRepository.findByAncestorConceptId(conceptAncestorId).map { it.toConceptAncestor() }
}

interface ConceptAncestorCrudRepository : CrudRepository<ConceptAncestorEntity, Int> {
    fun findByAncestorConceptId(conceptId: Int): List<ConceptAncestorEntity>
}
