package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.commons.asFhirSystemUri
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureCodeUnknown
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureGivingUp
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureInvalidCoding
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureSystemUnmappable
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessAlreadyHarmonized
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnAncestry
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnLocalMapping
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnRelation
import com.healthmetrix.qomop.harmonizer.search.ConceptByConceptCodeUseCase
import org.springframework.stereotype.Component

@Component
class CodingItemHarmonizeUseCase(
    private val conceptByConceptCodeUseCase: ConceptByConceptCodeUseCase,
    private val harmonizeByRelationUseCase: HarmonizeByRelationUseCase,
    private val harmonizeByAncestryUseCase: HarmonizeByAncestryUseCase,
    private val harmonizeByLocalMappingUseCase: HarmonizeByLocalMappingUseCase,
) {
    operator fun invoke(coding: Coding): HarmonizationResult {
        if (!coding.processable()) {
            return FailureInvalidCoding(coding)
        }

        val conceptFromLocal = harmonizeByLocalMappingUseCase(coding.system!!, coding.code!!)
        if (conceptFromLocal != null) {
            return SuccessOnLocalMapping(
                originalCoding = coding,
                harmonizedCoding = coding.applyConcept(conceptFromLocal),
                harmonized = conceptFromLocal.isHarmonized(),
                standardized = conceptFromLocal.isStandardized(),
            )
        }

        val vocabulary = coding.system.asFhirSystemUri() ?: return FailureSystemUnmappable(coding)

        // Note: the concept could still be invalid or non-standard in OMOP terms, however we ignore this and only check the vocabulary
        if (vocabulary.preferred) {
            return SuccessAlreadyHarmonized(coding)
        }

        val conceptByCode = conceptByConceptCodeUseCase(coding) ?: return FailureCodeUnknown(coding)

        val conceptByRelation = harmonizeByRelationUseCase(conceptByCode)
        if (conceptByRelation != null) {
            return SuccessOnRelation(
                originalCoding = coding,
                harmonizedCoding = coding.applyConcept(conceptByRelation),
                harmonized = conceptByRelation.isHarmonized(),
                standardized = conceptByRelation.isStandardized(),
            )
        }

        val conceptByAncestry = harmonizeByAncestryUseCase(conceptByCode)
        if (conceptByAncestry != null) {
            return SuccessOnAncestry(
                originalCoding = coding,
                harmonizedCoding = coding.applyConcept(conceptByAncestry),
                harmonized = conceptByAncestry.isHarmonized(),
                standardized = conceptByAncestry.isStandardized(),
            )
        }

        return FailureGivingUp(coding)
    }
}

fun Coding.processable(): Boolean = this.system != null && this.code != null
