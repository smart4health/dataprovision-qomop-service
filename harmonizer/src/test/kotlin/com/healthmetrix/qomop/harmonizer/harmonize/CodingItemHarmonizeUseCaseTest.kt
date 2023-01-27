package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.commons.test.TestUtils
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureCodeUnknown
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureGivingUp
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureInvalidCoding
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnAncestry
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnLocalMapping
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnRelation
import com.healthmetrix.qomop.harmonizer.search.ConceptByConceptCodeUseCase
import com.healthmetrix.qomop.omopcdm.api.Concept
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CodingItemHarmonizeUseCaseTest {
    private val testUtils = TestUtils()
    private val originalCoding = Coding("http://www.ama-assn.org/go/cpt", "code", "display")
    private val harmonizedCoding = Coding("http://loinc.org", "LA18277-6", "Cardiovascular")

    private val loincStandard: Concept = testUtils.fromJsonFile("concepts/loinc/standard.json")
    private val loincNonStandard: Concept = testUtils.fromJsonFile("concepts/loinc/non_standard.json")
    private val icdNonStandard: Concept = testUtils.fromJsonFile("concepts/icd10/non_standard.json")

    private val conceptByConceptCodeUseCase: ConceptByConceptCodeUseCase = mockk()
    private val harmonizeByRelationUseCase: HarmonizeByRelationUseCase = mockk()
    private val harmonizeByAncestryUseCase: HarmonizeByAncestryUseCase = mockk()
    private val harmonizeByLocalMappingUseCase: HarmonizeByLocalMappingUseCase = mockk()
    private val underTest =
        CodingItemHarmonizeUseCase(
            conceptByConceptCodeUseCase,
            harmonizeByRelationUseCase,
            harmonizeByAncestryUseCase,
            harmonizeByLocalMappingUseCase,
        )

    @Test
    fun `no concept code found`() {
        every { harmonizeByLocalMappingUseCase(any(), any()) } returns null
        every { conceptByConceptCodeUseCase(any()) } returns null

        val result = underTest(originalCoding)

        assertThat(result).isInstanceOf(FailureCodeUnknown::class.java)
        assertThat((result as FailureCodeUnknown).originalCoding).isEqualTo(originalCoding)
    }

    @Test
    fun `concept found by local mapping`() {
        every { harmonizeByLocalMappingUseCase(any(), any()) } returns loincStandard

        val result = underTest(originalCoding)

        assertThat(result).isInstanceOf(SuccessOnLocalMapping::class.java)
        assertThat((result as SuccessOnLocalMapping).harmonizedCoding).isEqualTo(harmonizedCoding)
        assertThat(result.harmonized).isTrue
        assertThat(result.standardized).isTrue
    }

    @Test
    fun `concept found by relation is harmonized and standardized`() {
        val foundConcept = icdNonStandard
        val mappedConcept = loincStandard

        every { harmonizeByLocalMappingUseCase(any(), any()) } returns null
        every { conceptByConceptCodeUseCase(originalCoding) } returns foundConcept
        every { harmonizeByRelationUseCase(foundConcept) } returns mappedConcept

        val result = underTest(originalCoding)

        assertThat(result).isInstanceOf(SuccessOnRelation::class.java)
        assertThat((result as SuccessOnRelation).harmonizedCoding).isEqualTo(harmonizedCoding)
        assertThat(result.harmonized).isTrue
        assertThat(result.standardized).isTrue
    }

    @Test
    fun `concept found by relation is not harmonized and not standardized`() {
        val foundConcept = icdNonStandard
        val mappedConcept = loincNonStandard

        every { harmonizeByLocalMappingUseCase(any(), any()) } returns null
        every { conceptByConceptCodeUseCase(originalCoding) } returns foundConcept
        every { harmonizeByRelationUseCase(foundConcept) } returns mappedConcept

        val result = underTest(originalCoding)

        assertThat(result).isInstanceOf(SuccessOnRelation::class.java)
        assertThat((result as SuccessOnRelation).harmonizedCoding).isEqualTo(harmonizedCoding)
        assertThat(result.harmonized).isFalse
        assertThat(result.standardized).isFalse
    }

    @Test
    fun `concept found by ancestors is non harmonized + non standardized`() {
        val foundConcept = icdNonStandard
        val mappedConcept = loincNonStandard

        every { harmonizeByLocalMappingUseCase(any(), any()) } returns null
        every { conceptByConceptCodeUseCase(originalCoding) } returns foundConcept
        every { harmonizeByRelationUseCase(any()) } returns null
        every { harmonizeByAncestryUseCase(foundConcept) } returns mappedConcept

        val result = underTest(originalCoding)

        assertThat(result).isInstanceOf(SuccessOnAncestry::class.java)
        assertThat((result as SuccessOnAncestry).harmonizedCoding).isEqualTo(harmonizedCoding)
        assertThat(result.harmonized).isFalse
        assertThat(result.standardized).isFalse
    }

    @Test
    fun `no concept found by relation or ancestors leads to FailureGivingUp`() {
        val foundConcept = icdNonStandard

        every { harmonizeByLocalMappingUseCase(any(), any()) } returns null
        every { conceptByConceptCodeUseCase(originalCoding) } returns foundConcept
        every { harmonizeByRelationUseCase(any()) } returns null
        every { harmonizeByAncestryUseCase(any()) } returns null

        val result = underTest(originalCoding)

        assertThat(result).isInstanceOf(FailureGivingUp::class.java)
        assertThat((result as FailureGivingUp).originalCoding).isEqualTo(originalCoding)
    }

    @Test
    fun `empty Coding leads to FailureInvalidCoding`() {
        val input = Coding(null, null)
        val result = underTest(input)
        assertThat((result as FailureInvalidCoding).originalCoding).isEqualTo(input)
    }

    @Test
    fun `Coding processable check`() {
        assertThat(Coding("system", "code", "display").processable()).isTrue

        val codingWithoutCode = Coding("system", null)
        assertThat(codingWithoutCode.processable()).isFalse

        val codingWithoutSystem = Coding(null, "code")
        assertThat(codingWithoutSystem.processable()).isFalse
    }
}
