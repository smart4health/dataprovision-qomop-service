package com.healthmetrix.qomop.persistence.harmonization

import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.sql.Timestamp
import java.util.UUID

@Entity
@Table(name = "coding_failed")
data class CodingFailedEntity(
    @Id
    val id: UUID,

    @Column(name = "system")
    val system: String,

    @Column(name = "code")
    val code: String,

    @Column(name = "display")
    val display: String?,

    @Column(name = "occurrence")
    val occurrence: Int,

    @Column(name = "concept_id")
    val conceptId: Int?,

    @Column(name = "valid_start")
    val validStart: Timestamp?,

    @Column(name = "valid_end")
    val validEnd: Timestamp?,
)

fun CodingFailedEntity.toFailedCoding() = CodingFailed(
    id = id,
    system = system,
    code = code,
    display = display,
    occurrence = occurrence,
    conceptId = conceptId,
    validStart = validStart?.toInstant(),
    validEnd = validEnd?.toInstant(),
)

fun CodingFailed.toEntity() = CodingFailedEntity(
    id = id,
    system = system,
    code = code,
    display = display,
    occurrence = occurrence,
    conceptId = conceptId,
    validStart = validStart?.toEpochMilli()?.let(::Timestamp),
    validEnd = validEnd?.toEpochMilli()?.let(::Timestamp),
)
