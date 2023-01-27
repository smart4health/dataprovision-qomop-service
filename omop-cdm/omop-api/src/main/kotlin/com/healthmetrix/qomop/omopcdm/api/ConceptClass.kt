package com.healthmetrix.qomop.omopcdm.api

data class ConceptClass(
    val id: String,
    val name: String,
    val conceptId: Int,
)

interface ConceptClassRepository {
    fun findById(conceptClassId: String): ConceptClass?
}
