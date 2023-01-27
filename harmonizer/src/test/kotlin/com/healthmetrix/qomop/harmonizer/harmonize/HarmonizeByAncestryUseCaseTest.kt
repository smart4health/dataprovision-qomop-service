package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.commons.test.TestUtils
import com.healthmetrix.qomop.omopcdm.api.Concept
import com.healthmetrix.qomop.omopcdm.api.ConceptRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HarmonizeByAncestryUseCaseTest {
    private val testUtils = TestUtils()

    private val loincStandard: Concept = testUtils.fromJsonFile("concepts/loinc/standard.json")
    private val icdStandard: Concept = testUtils.fromJsonFile("concepts/icd10/standard.json")
    private val loincNonStandard: Concept = testUtils.fromJsonFile("concepts/loinc/non_standard.json")
    private val icdNonStandard: Concept = testUtils.fromJsonFile("concepts/icd10/non_standard.json")
    private val unknownStandard: Concept = testUtils.fromJsonFile("concepts/unknown/standard.json")
    private val unknownNonStandard: Concept = testUtils.fromJsonFile("concepts/unknown/non_standard.json")

    private val conceptRepository: ConceptRepository = mockk()
    private val underTest = HarmonizeByAncestryUseCase(conceptRepository)

    @Nested
    inner class UseCaseLogic {
        @Test
        fun `is harmonized already`() {
            assertThat(underTest(loincStandard)).isEqualTo(loincStandard)
        }

        @Test
        fun `harmonized concept is part of ancestors`() {
            every { conceptRepository.findAncestryRelated(icdNonStandard.id) } returns listOf(
                icdStandard,
                loincStandard,
                unknownNonStandard,
            )
            assertThat(underTest(icdNonStandard)).isEqualTo(loincStandard)
        }

        @Test
        fun `harmonized concept is not part of ancestors, but standardized of ICD10 is`() {
            every { conceptRepository.findAncestryRelated(icdNonStandard.id) } returns listOf(
                icdStandard,
                unknownNonStandard,
                loincNonStandard,
                unknownStandard,
            )
            assertThat(underTest(icdNonStandard)).isEqualTo(icdStandard)
        }

        @Test
        fun `harmonized concept is not part of ancestors, but standardized of Loinc is`() {
            every { conceptRepository.findAncestryRelated(icdNonStandard.id) } returns listOf(
                icdStandard,
                unknownNonStandard,
                loincStandard,
                unknownStandard,
            )
            assertThat(underTest(icdNonStandard)).isEqualTo(loincStandard)
        }
    }

    @Nested
    inner class CustomConceptComparator {
        @Test
        fun `custom concept comparator`() {
            val unsortedList = listOf(
                unknownStandard,
                unknownNonStandard,
                loincStandard,
                loincNonStandard,
                icdStandard,
                icdNonStandard,
            )

            val underTest = CUSTOM_CONCEPT_COMPARATOR.reversed()
            val sortedList = unsortedList.sortedWith(underTest)
            assertThat(sortedList[0]).isEqualTo(loincStandard)
            assertThat(sortedList[1]).isEqualTo(icdStandard)
            assertThat(sortedList[2]).isEqualTo(loincNonStandard)
            assertThat(sortedList[3]).isEqualTo(icdNonStandard)
            assertThat(sortedList[4]).isEqualTo(unknownStandard)
            assertThat(sortedList[5]).isEqualTo(unknownNonStandard)
        }
    }
}
