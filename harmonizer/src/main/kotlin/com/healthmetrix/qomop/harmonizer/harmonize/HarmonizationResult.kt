package com.healthmetrix.qomop.harmonizer.harmonize

import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessAlreadyHarmonized
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnAncestry
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnLocalMapping
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizationResult.SuccessOnRelation

sealed class HarmonizationResult {

    abstract val originalCoding: Coding

    data class SuccessAlreadyHarmonized(
        override val originalCoding: Coding,
        val harmonized: Boolean = true,
        val standardized: Boolean = true,
    ) : HarmonizationResult()

    data class SuccessOnRelation(
        override val originalCoding: Coding,
        val harmonizedCoding: Coding,
        val harmonized: Boolean,
        val standardized: Boolean,
    ) : HarmonizationResult()

    data class SuccessOnAncestry(
        override val originalCoding: Coding,
        val harmonizedCoding: Coding,
        val harmonized: Boolean,
        val standardized: Boolean,
    ) : HarmonizationResult()

    data class SuccessOnLocalMapping(
        override val originalCoding: Coding,
        val harmonizedCoding: Coding,
        val harmonized: Boolean,
        val standardized: Boolean,
    ) : HarmonizationResult()

    data class FailureSystemUnmappable(
        override val originalCoding: Coding,
    ) : HarmonizationResult()

    data class FailureCodeUnknown(
        override val originalCoding: Coding,
    ) : HarmonizationResult()

    data class FailureGivingUp(
        override val originalCoding: Coding,
    ) : HarmonizationResult()

    data class FailureInvalidCoding(
        override val originalCoding: Coding,
    ) : HarmonizationResult()
}

fun HarmonizationResult.harmonizedCoding(): Coding? = when (this) {
    is SuccessOnAncestry -> harmonizedCoding
    is SuccessOnRelation -> harmonizedCoding
    is SuccessOnLocalMapping -> harmonizedCoding
    else -> null
}

fun HarmonizationResult.harmonized(): Boolean? = when (this) {
    is SuccessAlreadyHarmonized -> harmonized
    is SuccessOnAncestry -> harmonized
    is SuccessOnRelation -> harmonized
    is SuccessOnLocalMapping -> harmonized
    else -> null
}

fun HarmonizationResult.standardized(): Boolean? = when (this) {
    is SuccessAlreadyHarmonized -> standardized
    is SuccessOnAncestry -> standardized
    is SuccessOnRelation -> standardized
    is SuccessOnLocalMapping -> standardized
    else -> null
}

fun HarmonizationResult.asString(): String = when (this) {
    is SuccessOnAncestry -> "${javaClass.simpleName}, harmonized=$harmonized, standardized=$standardized"
    is SuccessOnRelation -> "${javaClass.simpleName}, harmonized=$harmonized, standardized=$standardized"
    is SuccessOnLocalMapping -> "${javaClass.simpleName}, harmonized=$harmonized, standardized=$standardized"
    is SuccessAlreadyHarmonized -> "${javaClass.simpleName}, harmonized=$harmonized, standardized=$standardized"
    else -> javaClass.simpleName
}
