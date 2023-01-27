package com.healthmetrix.qomop.harmonizer.feedback

import com.healthmetrix.qomop.harmonizer.controllers.ImportItem
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailedRepository
import com.healthmetrix.qomop.persistence.harmonization.api.isUnmapped
import com.healthmetrix.qomop.persistence.harmonization.api.isValid
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * 1. Invalidate existing CodingFailed entries
 * 2. Add new entries with occurrence=0 or update valid dates and conceptId
 */
@Component
class ImportCodingFailedItemUseCase(
    private val codingFailedRepository: CodingFailedRepository,
) {
    @Transactional
    operator fun invoke(item: ImportItem) {
        val (unmapped, existing) = codingFailedRepository.findAllBySystemAndCode(item.system, item.code)
            .partition { it.isUnmapped() }

        // Invalidate existing mappings
        existing.filter { it.isValid() }.forEach { codingFailedRepository.invalidate(it.id) }

        if (unmapped.isEmpty()) {
            // store initially, might happen when initially batch importing
            codingFailedRepository.save(
                CodingFailed(
                    id = UUID.randomUUID(),
                    system = item.system,
                    code = item.code,
                    display = item.display,
                    conceptId = item.targetConceptId,
                    occurrence = 0,
                    validStart = Instant.now(),
                    validEnd = null,
                ),
            )
        } else {
            // update the (most likely) only unmapped entry in the DB, otherwise we also don't care
            unmapped.forEach {
                codingFailedRepository.save(
                    it.copy(
                        conceptId = item.targetConceptId,
                        validStart = Instant.now(),
                        validEnd = null,
                    ),
                )
            }
        }
    }
}
