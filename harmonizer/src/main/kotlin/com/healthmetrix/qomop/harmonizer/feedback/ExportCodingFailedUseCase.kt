package com.healthmetrix.qomop.harmonizer.feedback

import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailedRepository
import com.healthmetrix.qomop.persistence.harmonization.api.isUnmapped
import com.healthmetrix.qomop.persistence.harmonization.api.isValid
import org.springframework.stereotype.Component

/**
 * Return either the unmapped codings only or the unmapped + the valid ones
 */
@Component
class ExportCodingFailedUseCase(
    private val codingFailedRepository: CodingFailedRepository,
) {
    operator fun invoke(all: Boolean = false): List<CodingFailed> {
        val groupedEntries = codingFailedRepository.findAll().groupBy { it.system to it.code }.values

        return if (all) {
            groupedEntries.mapNotNull { entries ->
                entries.firstOrNull { it.isValid() || it.isUnmapped() }
            }
        } else {
            groupedEntries.mapNotNull { entries -> entries.firstOrNull { it.isUnmapped() } }
        }
    }
}
