package com.healthmetrix.qomop.harmonizer.feedback

import com.healthmetrix.qomop.commons.SupportedVocabulary
import com.healthmetrix.qomop.harmonizer.controllers.ImportItem
import com.healthmetrix.qomop.omopcdm.api.Concept
import com.healthmetrix.qomop.omopcdm.api.Vocabulary
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailedRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ImportCodingFailedItemUseCaseTest {
    private val codingFailedRepository: CodingFailedRepository = mockk()
    private val underTest = ImportCodingFailedItemUseCase(codingFailedRepository)
    private val uuid = UUID.randomUUID()
    private val past = Instant.now().minusSeconds(10)
    private val importItem = ImportItem(
        "system",
        "code",
        null,
        1,
        "APPROVED",
    )

    @Test
    fun `import for valid item, no existing CodingFailed in our database, creating new one with zero occurrence`() {
        every { codingFailedRepository.findAllBySystemAndCode("system", "code") } returns emptyList()
        every { codingFailedRepository.save(any()) } returns Unit

        underTest(importItem)

        verify { codingFailedRepository.save(match { it.occurrence == 0 && it.conceptId != null }) }
    }

    @Test
    fun `import for valid item updates the existing unmapped item`() {
        val codingFailed = CodingFailed(uuid, "system", "code", null, 1, null, null, null)
        every { codingFailedRepository.findAllBySystemAndCode("system", "code") } returns listOf(codingFailed)
        every { codingFailedRepository.save(any()) } returns Unit

        underTest(importItem)

        verify {
            codingFailedRepository.save(
                match {
                    it.occurrence == 1 &&
                        it.conceptId != null &&
                        it.id == uuid &&
                        it.validStart!!.isBefore(Instant.now()) &&
                        it.validEnd == null
                },
            )
        }
    }

    @Test
    fun `invalidate existing mapping and create new one with zero occurrence`() {
        val mapped = CodingFailed(uuid, "system", "code", null, 3, 3, past, null)
        every { codingFailedRepository.findAllBySystemAndCode("system", "code") } returns listOf(mapped)
        every { codingFailedRepository.save(any()) } returns Unit
        every { codingFailedRepository.invalidate(any()) } returns Unit

        underTest(importItem)

        verify {
            codingFailedRepository.save(
                match {
                    it.occurrence == 0 &&
                        it.conceptId == 1 &&
                        it.id != uuid &&
                        it.validStart!!.isBefore(Instant.now()) &&
                        it.validEnd == null
                },
            )
        }

        verify { codingFailedRepository.invalidate(match { it == uuid }) }
    }

    private fun mockConcept(vocabulary: SupportedVocabulary): Concept =
        mockk<Concept>().also { concept ->
            every { concept.vocabulary } returns Vocabulary(id = vocabulary.omopVocabularyId, "", "", "", 2)
        }
}
