package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.commons.test.TestUtils
import com.healthmetrix.qomop.omopcdm.api.Concept
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HarmonizeUtilsKtTest {
    private val testUtils = TestUtils()

    private val loincStandard: Concept = testUtils.fromJsonFile("concepts/loinc/standard.json")
    private val icdStandard: Concept = testUtils.fromJsonFile("concepts/icd10/standard.json")
    private val icdNonStandard: Concept = testUtils.fromJsonFile("concepts/icd10/non_standard.json")
    private val unknownNonStandard: Concept = testUtils.fromJsonFile("concepts/unknown/non_standard.json")

    @Nested
    inner class CustomConceptAttributes {
        @Test
        fun `concept is from standard vocabulary, standardized, and vocabulary preferred aka harmonized`() {
            assertThat(loincStandard.isHarmonized()).isTrue
            assertThat(loincStandard.isStandardized()).isTrue
            assertThat(loincStandard.isMappable()).isTrue
        }

        @Test
        fun `concept is from standard vocabulary, standardized, but vocabulary not preferred aka not harmonized`() {
            assertThat(icdStandard.isHarmonized()).isFalse
            assertThat(icdStandard.isStandardized()).isTrue
            assertThat(icdStandard.isMappable()).isTrue
        }

        @Test
        fun `concept is from standard vocabulary, not standardized, but vocabulary not preferred aka not harmonized`() {
            assertThat(icdNonStandard.isHarmonized()).isFalse
            assertThat(icdNonStandard.isStandardized()).isFalse
            assertThat(icdNonStandard.isMappable()).isTrue
        }

        @Test
        fun `concept is not mappable for us`() {
            assertThat(unknownNonStandard.isHarmonized()).isFalse
            assertThat(unknownNonStandard.isStandardized()).isFalse
            assertThat(unknownNonStandard.isMappable()).isFalse
        }
    }

    @Test
    fun `custom copy from omop Concept to fhir Coding`() {
        val actual = Coding("system", "code", "display")

        val result = actual.applyConcept(loincStandard)

        assertThat(result.code).isEqualTo("LA18277-6")
        assertThat(result.system).isEqualTo("http://loinc.org")
        assertThat(result.display).isEqualTo("Cardiovascular")
    }
}
