package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.omopcdm.api.Concept
import com.healthmetrix.qomop.omopcdm.api.ConceptRepository
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailedRepository
import com.healthmetrix.qomop.persistence.harmonization.api.isValid
import org.springframework.stereotype.Component

@Component
class HarmonizeByLocalMappingUseCase(
    private val codingFailedRepository: CodingFailedRepository,
    private val conceptRepository: ConceptRepository,
) {
    operator fun invoke(system: String, code: String): Concept? =
        codingFailedRepository.findAllBySystemAndCode(system, code)
            .filter { it.isValid() }
            .maxByOrNull { it.validStart!! }
            ?.conceptId?.let { conceptRepository.findById(it) }
}
