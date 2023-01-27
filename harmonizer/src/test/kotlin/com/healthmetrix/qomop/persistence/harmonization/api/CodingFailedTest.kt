package com.healthmetrix.qomop.persistence.harmonization.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CodingFailedTest {

    private val past = Instant.now().minusSeconds(10)
    private val future = Instant.now().plusSeconds(10)
    private val uuid = UUID.randomUUID()

    private val unmapped = CodingFailed(uuid, "system", "code", null, 1, null, null, null)
    private val valid = CodingFailed(uuid, "system", "code", null, 1, 1, past, null)
    private val validWithFuture = CodingFailed(uuid, "system", "code", null, 1, 1, past, future)
    private val invalidated = CodingFailed(uuid, "system", "code", null, 1, 1, past, past)

    @Test
    fun valid() {
        assertThat(valid.isValid()).isTrue
        assertThat(validWithFuture.isValid()).isTrue
        assertThat(unmapped.isValid()).isFalse
        assertThat(invalidated.isValid()).isFalse
    }

    @Test
    fun unmapped() {
        assertThat(valid.isUnmapped()).isFalse
        assertThat(validWithFuture.isUnmapped()).isFalse
        assertThat(unmapped.isUnmapped()).isTrue
        assertThat(invalidated.isUnmapped()).isFalse
    }
}
