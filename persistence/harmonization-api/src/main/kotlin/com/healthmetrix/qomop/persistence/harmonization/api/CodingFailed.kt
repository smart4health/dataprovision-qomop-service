package com.healthmetrix.qomop.persistence.harmonization.api

import java.time.Instant
import java.util.UUID

data class CodingFailed(
    val id: UUID,
    val system: String,
    val code: String,
    val display: String?,
    val occurrence: Int,
    val conceptId: Int?,
    val validStart: Instant?,
    val validEnd: Instant?,
)

interface CodingFailedRepository {
    fun save(codingFailed: CodingFailed)
    fun findAll(): List<CodingFailed>
    fun findAllBySystemAndCode(system: String, code: String): List<CodingFailed>
    fun incrementOccurrence(id: UUID)
    fun invalidate(id: UUID)
}

fun CodingFailed.isValid() =
    conceptId != null &&
        validStart?.isBefore(Instant.now()) ?: false &&
        validEnd?.isAfter(Instant.now()) ?: true

fun CodingFailed.isUnmapped() = conceptId == null && validStart == null && validEnd == null
