package com.healthmetrix.qomop.omopcdm.entity

import com.healthmetrix.qomop.omopcdm.api.Domain
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * See OMOP CDMv6 documentation on this entity: [https://ohdsi.github.io/CommonDataModel/cdm60.html#DOMAIN]
 */
@Entity
@Table(name = "domain")
data class DomainEntity(
    @Id
    @Column(name = "domain_id")
    val id: String,

    @Column(name = "domain_name")
    val name: String,

    @Column(name = "domain_concept_id")
    val conceptClassId: Int,
)

fun DomainEntity.toDomain() = Domain(
    id = id,
    name = name,
    conceptClassId = conceptClassId,
)
