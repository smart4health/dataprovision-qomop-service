package com.healthmetrix.qomop.omopcdm.repository

import com.healthmetrix.qomop.omopcdm.api.ConceptClass
import com.healthmetrix.qomop.omopcdm.api.ConceptClassRepository
import com.healthmetrix.qomop.omopcdm.entity.ConceptClassEntity
import com.healthmetrix.qomop.omopcdm.entity.toConceptClass
import org.springframework.data.repository.CrudRepository

class RemoteConceptClassRepository(
    private val conceptClassRepository: ConceptClassCrudRepository,
) : ConceptClassRepository {

    override fun findById(conceptClassId: String): ConceptClass? =
        conceptClassRepository.findById(conceptClassId).orElse(null)?.toConceptClass()
}

interface ConceptClassCrudRepository : CrudRepository<ConceptClassEntity, String>
