package com.healthmetrix.qomop.harmonizer.feedback

import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailedRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ExportCodingFailedUseCaseTest {
    private val codingFailedRepository: CodingFailedRepository = mockk()
    private val underTest = ExportCodingFailedUseCase(codingFailedRepository)
    private val past = Instant.now().minusSeconds(10)

    private val unmapped = CodingFailed(UUID.randomUUID(), "system1", "code1", null, 1, null, null, null)
    private val valid = CodingFailed(UUID.randomUUID(), "system2", "code2", null, 1, 1, past, null)
    private val invalidated = CodingFailed(UUID.randomUUID(), "system2", "code2", null, 1, 1, past, past)

    @Test
    fun `test nothing found`() {
        every { codingFailedRepository.findAll() } returns emptyList()
        underTest().let { assertThat(it).isEmpty() }
    }

    @Test
    fun `getting only unmapped codings`() {
        every { codingFailedRepository.findAll() } returns listOf(unmapped, valid, invalidated)
        underTest().let { assertThat(it).containsOnly(unmapped) }
    }

    @Test
    fun `getting both valid and unmapped codings`() {
        every { codingFailedRepository.findAll() } returns listOf(unmapped, valid, invalidated)
        underTest(true).let {
            assertThat(it).containsAll(listOf(unmapped, valid))
        }
    }
}
