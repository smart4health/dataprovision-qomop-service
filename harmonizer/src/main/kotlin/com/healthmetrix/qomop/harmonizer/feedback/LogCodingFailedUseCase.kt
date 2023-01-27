package com.healthmetrix.qomop.harmonizer.feedback

import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureInvalidCoding
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailedRepository
import com.healthmetrix.qomop.persistence.harmonization.api.isUnmapped
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 1. Checks if the entry should be persisted based on the HarmonizationResult type
 * 2. Add an entry if none existing, otherwise increment
 * 3. Increments the occurrence of invalid items so we can keep track of those
 */
@Component
class LogCodingFailedUseCase(
    private val codingFailedRepository: CodingFailedRepository,
) {

    @Transactional
    operator fun invoke(harmonizationResult: HarmonizationResult) {
        if (harmonizationResult.isSuccess() || harmonizationResult is FailureInvalidCoding) {
            return
        }
        val coding = harmonizationResult.originalCoding
        val existingEntry = codingFailedRepository.findAllBySystemAndCode(
            coding.system!!,
            coding.code!!,
        )
        if (existingEntry.isEmpty()) {
            codingFailedRepository.save(
                CodingFailed(
                    id = UUID.randomUUID(),
                    system = coding.system,
                    code = coding.code,
                    display = coding.display,
                    conceptId = null,
                    occurrence = 1,
                    validEnd = null,
                    validStart = null,
                ),
            )
        } else {
            // should only be one by implementation anyways
            existingEntry
                .filter { it.isUnmapped() }
                .forEach { codingFailedRepository.incrementOccurrence(it.id) }
        }
    }
}

fun HarmonizationResult.isSuccess(): Boolean = when (this) {
    is HarmonizationResult.SuccessOnAncestry -> true
    is HarmonizationResult.SuccessOnRelation -> true
    is HarmonizationResult.SuccessAlreadyHarmonized -> true
    is HarmonizationResult.SuccessOnLocalMapping -> true
    else -> false
}
