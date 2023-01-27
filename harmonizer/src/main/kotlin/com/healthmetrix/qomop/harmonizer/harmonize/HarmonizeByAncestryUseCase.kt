package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.omopcdm.api.Concept
import com.healthmetrix.qomop.omopcdm.api.ConceptRepository
import org.springframework.stereotype.Component

@Component
class HarmonizeByAncestryUseCase(
    private val conceptRepository: ConceptRepository,
) {
    operator fun invoke(input: Concept): Concept? {
        if (input.isHarmonized()) {
            return input
        }
        val (harmonizedList, standardizedList) = conceptRepository.findAncestryRelated(input.id)
            .filter { it.isStandardized() }
            .partition { it.isHarmonized() }

        return harmonizedList.firstOrNull()
            ?: standardizedList.sortedWith(CUSTOM_CONCEPT_COMPARATOR).asReversed().firstOrNull()
    }
}

/**
 * Comparator sorting concepts in ascending order in terms of harmonization
 */
val CUSTOM_CONCEPT_COMPARATOR: Comparator<Concept> = Comparator { o1, o2 ->
    when {
        o1.isHarmonized() -> {
            if (o2.isHarmonized()) {
                0
            } else {
                1
            }
        }
        o1.isStandardized() -> {
            if (o2.isStandardized()) {
                0
            } else {
                1
            }
        }
        o1.isMappable() -> {
            if (o2.isStandardized()) {
                -1
            } else if (o2.isMappable()) {
                0
            } else {
                1
            }
        }
        else -> {
            if (o2.isMappable()) {
                -1
            } else {
                0
            }
        }
    }
}
