package com.healthmetrix.qomop.persistence.harmonization

import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailedRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Component
class RemoteCodingFailedRepository(
    private val crudRepository: RemoteCodingFailedCrudRepository,
) : CodingFailedRepository {
    override fun save(codingFailed: CodingFailed) {
        crudRepository.save(codingFailed.toEntity())
    }

    override fun findAll(): List<CodingFailed> =
        crudRepository.findAll().toList().map { it.toFailedCoding() }

    override fun findAllBySystemAndCode(system: String, code: String): List<CodingFailed> =
        crudRepository.findBySystemAndCode(system, code).map { it.toFailedCoding() }

    override fun incrementOccurrence(id: UUID) {
        crudRepository.findById(id).ifPresent {
            crudRepository.save(it.copy(occurrence = it.occurrence + 1))
        }
    }

    override fun invalidate(id: UUID) {
        crudRepository.findById(id).ifPresent {
            crudRepository.save(it.copy(validEnd = Instant.now().toEpochMilli().let(::Timestamp)))
        }
    }
}

interface RemoteCodingFailedCrudRepository : CrudRepository<CodingFailedEntity, UUID> {
    fun findBySystemAndCode(system: String, code: String): List<CodingFailedEntity>
}
