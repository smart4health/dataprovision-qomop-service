package com.healthmetrix.qomop.harmonizer.controllers

import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.merge
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import com.healthmetrix.qomop.commons.ApiResponse
import com.healthmetrix.qomop.commons.DocumentationConstants
import com.healthmetrix.qomop.commons.asEntity
import com.healthmetrix.qomop.commons.logger
import com.healthmetrix.qomop.harmonizer.feedback.LogCodingFailedUseCase
import com.healthmetrix.qomop.harmonizer.harmonize.Coding
import com.healthmetrix.qomop.harmonizer.harmonize.CodingItemHarmonizeUseCase
import com.healthmetrix.qomop.harmonizer.harmonize.asString
import com.healthmetrix.qomop.harmonizer.harmonize.harmonized
import com.healthmetrix.qomop.harmonizer.harmonize.harmonizedCoding
import com.healthmetrix.qomop.harmonizer.harmonize.standardized
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as DocRequestBody

@RestController
@RequestMapping("/v1/harmonizer")
@SecurityRequirement(name = DocumentationConstants.BASIC_AUTH)
@Tag(name = DocumentationConstants.HARMONIZATION_API_TAG)
class HarmonizationController(
    private val codingItemHarmonizeUseCase: CodingItemHarmonizeUseCase,
    private val logCodingFailedUseCase: LogCodingFailedUseCase,
) {

    @Operation(
        summary = "Given a FHIR coding pair, tries to map it to one of our supported standard " +
            "vocabularies and return the result. Failed harmonizations are logged in a coding_failed table that " +
            "can manually be re-harmonized.",
    )
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "The coding was traced successfully indicating either true/false for the category match.",
                content = [Content(schema = Schema(implementation = HarmonizationResponse.HarmonizationDone::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "The system-code pair is not found in Omop.",
                content = [Content(schema = Schema(implementation = HarmonizationResponse.HarmonizationError::class))],
            ),
        ],
    )
    @PostMapping(
        "/coding",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun harmonizeCoding(
        @DocRequestBody(description = "Contains a pair of system and code that's available in OMOP as Concept and Vocabulary")
        @RequestBody
        coding: Coding,
    ): ResponseEntity<HarmonizationResponse> = runCatching {
        codingItemHarmonizeUseCase(coding).also { logCodingFailedUseCase(it) }
    }.onFailure { logger.warn("Harmonization failed for $coding", it) }
        .mapError { HarmonizationResponse.HarmonizationError("${it.javaClass.name}: ${it.message}") }
        .map {
            HarmonizationResponse.HarmonizationDone(
                success = it.harmonized() == null,
                message = it.asString(),
                harmonizedCoding = it.harmonizedCoding(),
                harmonized = it.harmonized(),
                standardized = it.standardized(),
            )
        }
        .merge().asEntity()

    sealed class HarmonizationResponse : ApiResponse {
        data class HarmonizationDone(
            val success: Boolean,
            val message: String,
            val harmonizedCoding: Coding?,
            val harmonized: Boolean?,
            val standardized: Boolean?,
        ) : HarmonizationResponse()

        data class HarmonizationError(
            val message: String,
        ) : HarmonizationResponse() {
            override val status = HttpStatus.BAD_REQUEST
        }
    }
}
