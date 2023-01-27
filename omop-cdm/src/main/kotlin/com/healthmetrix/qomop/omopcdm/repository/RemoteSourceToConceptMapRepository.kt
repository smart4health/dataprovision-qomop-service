package com.healthmetrix.qomop.omopcdm.repository

import com.healthmetrix.qomop.omopcdm.api.SourceToConceptMap
import com.healthmetrix.qomop.omopcdm.api.SourceToConceptMapRepository
import com.healthmetrix.qomop.omopcdm.entity.SourceToConceptMapEntity
import com.healthmetrix.qomop.omopcdm.entity.toSourceToConceptMap
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
class RemoteSourceToConceptMapRepository(
    private val sourceToConceptMapRepository: SourceToConceptMapCrudRepository,
) : SourceToConceptMapRepository {

    override fun findBySourceConceptCode(sourceConceptCode: String): SourceToConceptMap? =
        sourceToConceptMapRepository.findById(sourceConceptCode).orElse(null)?.toSourceToConceptMap()
}

interface SourceToConceptMapCrudRepository : CrudRepository<SourceToConceptMapEntity, String>
