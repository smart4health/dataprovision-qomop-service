package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.commons.SupportedVocabulary
import com.healthmetrix.qomop.commons.asFhirSystemUri
import com.healthmetrix.qomop.commons.asOmopVocabularyId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SupportedVocabularyTest {
    private var supportedVocabulary = SupportedVocabulary.SNOMED

    @Test
    fun `find fromFhirSystemUri successful`() {
        assertThat(supportedVocabulary.fhirSystemUri.asFhirSystemUri()).isEqualTo(supportedVocabulary)
    }

    @Test
    fun `find fromFhirSystemUri failed`() {
        assertThat("".asFhirSystemUri()).isNull()
    }

    @Test
    fun `find fromOmopVocabularyId successful`() {
        assertThat(supportedVocabulary.omopVocabularyId.asOmopVocabularyId()).isEqualTo(supportedVocabulary)
    }

    @Test
    fun `find fromOmopVocabularyId failed`() {
        assertThat("".asOmopVocabularyId()).isNull()
    }
}
