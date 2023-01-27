package com.healthmetrix.qomop.omopcdm.repository

import com.healthmetrix.qomop.omopcdm.api.Domain
import com.healthmetrix.qomop.omopcdm.api.DomainRepository
import com.healthmetrix.qomop.omopcdm.entity.DomainEntity
import com.healthmetrix.qomop.omopcdm.entity.toDomain
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
class RemoteDomainRepository(
    private val domainRepository: DomainCrudRepository,
) : DomainRepository {

    override fun findById(domainId: String): Domain? =
        domainRepository.findById(domainId).orElse(null)?.toDomain()
}

interface DomainCrudRepository : CrudRepository<DomainEntity, String>
