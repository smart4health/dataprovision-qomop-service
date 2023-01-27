package com.healthmetrix.qomop.harmonizer.search

import com.healthmetrix.qomop.commons.asFhirSystemUri
import com.healthmetrix.qomop.commons.asOmopVocabularyId
import com.healthmetrix.qomop.harmonizer.harmonize.Coding
import com.healthmetrix.qomop.omopcdm.api.Concept
import com.healthmetrix.qomop.omopcdm.api.ConceptRepository
import org.springframework.stereotype.Component

@Component
class ConceptByConceptCodeUseCase(
    private val conceptRepository: ConceptRepository,
) {
    operator fun invoke(
        coding: Coding,
    ): Concept? = conceptRepository
        .findByConceptCode(coding.code!!)
        .firstOrNull { coding.system?.asFhirSystemUri() == it.vocabulary.id.asOmopVocabularyId() }
}
