package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureCodeUnknown
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureGivingUp
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureInvalidCoding
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.FailureSystemUnmappable
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessAlreadyHarmonized
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnAncestry
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnRelation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HarmonizationResultTest {
    private val coding = Coding("http://loinc.org", "LA18277-6", "Cardiovascular")
    private val harmonized = Coding("http://loinc.org", "LA18277-7", "something else")

    private val successOnAncestry = SuccessOnAncestry(coding, harmonized, harmonized = false, standardized = false)
    private val successOnRelation = SuccessOnRelation(coding, harmonized, harmonized = true, standardized = true)
    private val successAlreadyHarmonized = SuccessAlreadyHarmonized(coding, harmonized = true, standardized = false)
    private val failureGivingUp = FailureGivingUp(coding)
    private val failureCodeUnknown = FailureCodeUnknown(coding)
    private val failureSystemUnmappable = FailureSystemUnmappable(coding)
    private val failureInvalidCoding = FailureInvalidCoding(coding)

    @Test
    fun `logMessage on HarmonizationResult`() {
        assertThat(successOnAncestry.asString()).isEqualTo("SuccessOnAncestry, harmonized=false, standardized=false")
        assertThat(successOnRelation.asString()).isEqualTo("SuccessOnRelation, harmonized=true, standardized=true")
        assertThat(successAlreadyHarmonized.asString()).isEqualTo("SuccessAlreadyHarmonized, harmonized=true, standardized=false")
        assertThat(failureGivingUp.asString()).isEqualTo("FailureGivingUp")
        assertThat(failureCodeUnknown.asString()).isEqualTo("FailureCodeUnknown")
        assertThat(failureSystemUnmappable.asString()).isEqualTo("FailureSystemUnmappable")
        assertThat(failureInvalidCoding.asString()).isEqualTo("FailureInvalidCoding")
    }

    @Test
    fun `targetCoding on HarmonizationResult`() {
        assertThat(successOnAncestry.harmonizedCoding()).isEqualTo(harmonized)
        assertThat(successOnRelation.harmonizedCoding()).isEqualTo(harmonized)
        assertThat(successAlreadyHarmonized.harmonizedCoding()).isNull()
        assertThat(failureGivingUp.harmonizedCoding()).isNull()
        assertThat(failureCodeUnknown.harmonizedCoding()).isNull()
        assertThat(failureSystemUnmappable.harmonizedCoding()).isNull()
        assertThat(failureInvalidCoding.harmonizedCoding()).isNull()
    }
}
