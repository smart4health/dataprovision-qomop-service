package com.healthmetrix.qomop.harmonizer.feedback

import com.healthmetrix.qomop.commons.SupportedVocabulary
import com.healthmetrix.qomop.harmonizer.controllers.ImportItem
import com.healthmetrix.qomop.omopcdm.api.Concept
import com.healthmetrix.qomop.omopcdm.api.ConceptRepository
import com.healthmetrix.qomop.omopcdm.api.Vocabulary
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ImportAllCodingFailedUseCaseTest {
    private val importCodingFailedItemUseCase: ImportCodingFailedItemUseCase = mockk()
    private val conceptRepository: ConceptRepository = mockk()
    private val underTest = ImportAllCodingFailedUseCase(importCodingFailedItemUseCase, conceptRepository)
    private val preferredVocabulary = SupportedVocabulary.SNOMED
    private val skippedVocabulary = SupportedVocabulary.ICD9CM

    @Test
    fun `import for empty list`() {
        underTest(emptyList()).let {
            assertThat(it.totalSize).isZero
            assertThat(it.skipped).isEmpty()
            assertThat(it.imported).isEmpty()
        }
    }

    @Test
    fun `import for empty item being skipped`() {
        underTest(listOf(ImportItem("system", "code", null, null, null))).let {
            assertThat(it.totalSize).isOne
            assertThat(it.skipped).hasSize(1)
            assertThat(it.imported).isEmpty()
        }
    }

    @Test
    fun `import for non preferred vocabulary`() {
        every { conceptRepository.findById(1) } returns mockConcept(skippedVocabulary)
        underTest(importItemList(skippedVocabulary)).let {
            assertThat(it.totalSize).isOne
            assertThat(it.skipped).hasSize(1)
            assertThat(it.imported).isEmpty()
        }
    }

    @Test
    fun `import for non approved mapping`() {
        every { conceptRepository.findById(1) } returns mockConcept(preferredVocabulary)
        underTest(importItemList(preferredVocabulary).map { it.copy(mappingStatus = "UNMAPPED") }).let {
            assertThat(it.totalSize).isOne
            assertThat(it.skipped).hasSize(1)
            assertThat(it.imported).isEmpty()
        }
    }

    @Test
    fun `import for valid item, but having no matching omop concept in our database`() {
        every { conceptRepository.findById(1) } returns null
        underTest(importItemList(preferredVocabulary)).let {
            assertThat(it.totalSize).isOne
            assertThat(it.skipped).hasSize(1)
            assertThat(it.imported).isEmpty()
        }
    }

    @Test
    fun `import for valid item, use case for import called`() {
        every { conceptRepository.findById(1) } returns mockConcept(preferredVocabulary)
        every { importCodingFailedItemUseCase(any()) } just runs
        underTest(importItemList(preferredVocabulary)).let {
            assertThat(it.totalSize).isOne
            assertThat(it.skipped).isEmpty()
            assertThat(it.imported).hasSize(1)
        }
    }

    private fun mockConcept(vocabulary: SupportedVocabulary): Concept =
        mockk<Concept>().also { concept ->
            every { concept.vocabulary } returns Vocabulary(id = vocabulary.omopVocabularyId, "", "", "", 2)
        }

    private fun importItemList(vocabulary: SupportedVocabulary) =
        listOf(
            ImportItem(
                vocabulary.fhirSystemUri,
                "code",
                null,
                1,
                "APPROVED",
            ),
        )
}
