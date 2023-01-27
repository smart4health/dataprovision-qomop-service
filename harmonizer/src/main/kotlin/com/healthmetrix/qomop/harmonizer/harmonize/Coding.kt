package com.healthmetrix.qomop.harmonizer.harmonize

/**
 * Is actually org.hl7.fhir.r4.model.Coding
 */
data class Coding(
    val system: String?,
    val code: String?,
    val display: String? = null,
)
