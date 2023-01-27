package com.healthmetrix.qomop.omopcdm.repository

import com.healthmetrix.qomop.omopcdm.api.Vocabulary
import com.healthmetrix.qomop.omopcdm.api.VocabularyRepository
import com.healthmetrix.qomop.omopcdm.entity.VocabularyEntity
import com.healthmetrix.qomop.omopcdm.entity.toVocabulary
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component

@Component
class RemoteVocabularyRepository(
    private val vocabularyRepository: VocabularyCrudRepository,
) : VocabularyRepository {

    override fun findById(vocabularyId: String): Vocabulary? =
        vocabularyRepository.findById(vocabularyId).orElse(null)?.toVocabulary()

    override fun findByName(name: String): List<Vocabulary> =
        vocabularyRepository.findByName(name).map { it.toVocabulary() }
}

interface VocabularyCrudRepository : CrudRepository<VocabularyEntity, String> {
    fun findByName(name: String): List<VocabularyEntity>
}
