package com.healthmetrix.qomop.harmonizer.feedback

import com.healthmetrix.qomop.commons.asOmopVocabularyId
import com.healthmetrix.qomop.harmonizer.controllers.ImportItem
import com.healthmetrix.qomop.omopcdm.api.ConceptRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * For each import item:
 * 1. Check constraints and skip if not met
 * 2. Run Import and return ImportResult
 */
@Component
class ImportAllCodingFailedUseCase(
    private val importCodingFailedItemUseCase: ImportCodingFailedItemUseCase,
    private val conceptRepository: ConceptRepository,
) {
    @Transactional
    operator fun invoke(importItems: List<ImportItem>): ImportResult {
        val (imported, skipped) = importItems.partition {
            it.targetConceptId != null && it.targetConceptId > 0 &&
                it.mappingStatus == "APPROVED" &&
                conceptRepository.findById(conceptId = it.targetConceptId)?.vocabulary?.id?.asOmopVocabularyId()?.preferred ?: false
        }

        imported.forEach { importCodingFailedItemUseCase(it) }

        return ImportResult(
            totalSize = importItems.size,
            imported = imported,
            skipped = skipped,
        )
    }
}

data class ImportResult(
    val imported: List<ImportItem>,
    val skipped: List<ImportItem>,
    val totalSize: Int,
)
