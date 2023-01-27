package com.healthmetrix.qomop.omopcdm.api

data class Domain(
    val id: String,
    val name: String,
    val conceptClassId: Int,
)

interface DomainRepository {
    fun findById(domainId: String): Domain?
}
