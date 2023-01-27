package com.healthmetrix.qomop.tracer.controllers

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.merge
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.toErrorIf
import com.github.michaelbull.result.toResultOr
import com.healthmetrix.qomop.commons.ApiResponse
import com.healthmetrix.qomop.commons.DocumentationConstants
import com.healthmetrix.qomop.commons.asEntity
import com.healthmetrix.qomop.commons.logger
import com.healthmetrix.qomop.tracer.DataSelectionCategory
import com.healthmetrix.qomop.tracer.usecases.FitnessDataConceptTracer
import com.healthmetrix.qomop.tracer.usecases.ImplantsConceptTracer
import com.healthmetrix.qomop.tracer.usecases.LaboratoryConceptTracer
import com.healthmetrix.qomop.tracer.usecases.SurgeriesConceptTracer
import com.healthmetrix.qomop.tracer.usecases.TracingError
import com.healthmetrix.qomop.tracer.usecases.VitalSignsConceptTracer
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as DocRequestBody

val tracingEnabledFor = setOf(
    DataSelectionCategory.PROCEDURES_SURGERIES,
    DataSelectionCategory.MEDICAL_DEVICES_IMPLANTS,
    DataSelectionCategory.VITAL_SIGNS,
    DataSelectionCategory.DIAGNOSTIC_RESULTS_LABORATORY,
    DataSelectionCategory.DIAGNOSTIC_RESULTS_FITNESS,
)

@RestController
@RequestMapping("/v1/tracer")
@SecurityRequirement(name = DocumentationConstants.BASIC_AUTH)
@Tag(name = DocumentationConstants.CONCEPT_TRACING_API_TAG)
class ConceptTracingController(
    private val surgeriesConceptTracer: SurgeriesConceptTracer,
    private val implantsConceptTracer: ImplantsConceptTracer,
    private val laboratoryConceptTracer: LaboratoryConceptTracer,
    private val vitalSignsConceptTracer: VitalSignsConceptTracer,
    private val fitnessDataConceptTracer: FitnessDataConceptTracer,
) {

    @Operation(summary = "Given a data category by its ID, traces the provided coding if it is belonging to that category or not.")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "The coding was traced successfully indicating either true/false for the category match.",
                content = [Content(schema = Schema(implementation = DataSelectionResponse.Traced::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "The system-code pair is not found in Omop.",
                content = [Content(schema = Schema(implementation = DataSelectionResponse.NotFound::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "The categoryId is not matching a category or the tracing isn't enabled for it.",
                content = [Content(schema = Schema(implementation = DataSelectionResponse.UnsupportedCategory::class))],
            ),
        ],
    )
    @PostMapping(
        "/data-category/{categoryId}",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun traceConceptForCategory(
        @Parameter(description = "one of 5,6,7,8,9", `in` = ParameterIn.PATH)
        @PathVariable
        categoryId: Int,
        @DocRequestBody(description = "A pair of system-code that depending on the category originates from its key indicators in the FHIR resource.")
        @RequestBody
        queryItem: QueryBody,
    ): ResponseEntity<DataSelectionResponse> = binding<DataSelectionResponse.Traced, DataSelectionResponse> {
        val category = DataSelectionCategory.fromId(value = categoryId)
            .toResultOr { DataSelectionResponse.UnsupportedCategory() }
            .toErrorIf(
                predicate = { !tracingEnabledFor.contains(it) },
                transform = { DataSelectionResponse.UnsupportedCategory() },
            )
            .bind()

        when (category) {
            DataSelectionCategory.PROCEDURES_SURGERIES -> surgeriesConceptTracer.contains(queryItem)
            DataSelectionCategory.MEDICAL_DEVICES_IMPLANTS -> implantsConceptTracer.contains(queryItem)
            DataSelectionCategory.VITAL_SIGNS -> vitalSignsConceptTracer.contains(queryItem)
            DataSelectionCategory.DIAGNOSTIC_RESULTS_LABORATORY -> laboratoryConceptTracer.contains(queryItem)
            DataSelectionCategory.DIAGNOSTIC_RESULTS_FITNESS -> fitnessDataConceptTracer.contains(queryItem)
            else -> throw IllegalStateException("Category $category is not configured!")
        }
            .onFailure { it.log(queryItem) }
            .mapError {
                when (it) {
                    TracingError.ConceptNotFound -> DataSelectionResponse.NotFound(
                        message = "The item $queryItem could not be found in OMOP",
                        match = false,
                    )
                }
            }.bind().let { DataSelectionResponse.Traced(it) }
    }.merge().asEntity()

    sealed class DataSelectionResponse : ApiResponse {
        data class Traced(
            val match: Boolean,
        ) : DataSelectionResponse()

        data class NotFound(
            val message: String,
            val match: Boolean,
        ) : DataSelectionResponse() {
            override val status = HttpStatus.OK
        }

        data class UnsupportedCategory(
            val message: String = "Invalid categoryId supplied. Use one of $tracingEnabledFor",
        ) : DataSelectionResponse() {
            override val status = HttpStatus.BAD_REQUEST
        }
    }
}

/**
 * Is actually org.hl7.fhir.r4.model.Coding.
 * Maps to Concept.vocabulary and Concept.code in OMOP
 */
data class QueryBody(
    val system: String,
    val code: String,
)

private fun TracingError.log(queryItem: QueryBody) = when (this) {
    TracingError.ConceptNotFound -> logger.warn(
        "Coding $queryItem was not found in OMOP, returning match=false. Check if it exists!",
    )
}
