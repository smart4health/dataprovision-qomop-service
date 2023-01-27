package com.healthmetrix.qomop.omopcdm.api

data class Vocabulary(
    val id: String,
    val name: String,
    val reference: String?,
    val version: String?,
    val conceptId: Int,
)

interface VocabularyRepository {
    fun findById(vocabularyId: String): Vocabulary?
    fun findByName(name: String): List<Vocabulary>
}
