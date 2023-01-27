package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.commons.asOmopVocabularyId
import com.healthmetrix.qomop.omopcdm.api.Concept

fun Concept.isHarmonized(): Boolean {
    return this.isStandardized() && this.vocabulary.id.asOmopVocabularyId()?.preferred ?: false
}

fun Concept.isStandardized(): Boolean {
    return this.standardConcept && this.isMappable()
}

fun Concept.isMappable(): Boolean {
    return this.vocabulary.id.asOmopVocabularyId() != null
}

fun Coding.applyConcept(concept: Concept): Coding {
    return copy(
        code = concept.conceptCode,
        system = concept.vocabulary.id.asOmopVocabularyId()!!.fhirSystemUri,
        display = concept.name,
    )
}
