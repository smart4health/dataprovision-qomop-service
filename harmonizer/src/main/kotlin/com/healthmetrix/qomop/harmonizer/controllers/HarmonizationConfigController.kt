package com.healthmetrix.qomop.harmonizer.controllers

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.merge
import com.healthmetrix.qomop.commons.ApiResponse
import com.healthmetrix.qomop.commons.DocumentationConstants
import com.healthmetrix.qomop.commons.asEntity
import com.healthmetrix.qomop.commons.orThrow
import com.healthmetrix.qomop.harmonizer.feedback.ExportCodingFailedUseCase
import com.healthmetrix.qomop.harmonizer.feedback.ImportAllCodingFailedUseCase
import com.healthmetrix.qomop.harmonizer.feedback.ImportResult
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import com.opencsv.CSVReaderBuilder
import com.opencsv.CSVWriter
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvToBeanBuilder
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.InputStreamReader
import java.io.StringWriter
import com.github.michaelbull.result.runCatching as catch
import io.swagger.v3.oas.annotations.parameters.RequestBody as DocRequestBody

@Tag(name = DocumentationConstants.CODING_EXCHANGE_API_TAG)
@SecurityRequirement(name = DocumentationConstants.BASIC_AUTH)
@ApiResponses(
    value = [
        io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized action",
            content = [Content()],
        ),
    ],
)
@RestController
@RequestMapping("/v1/harmonization-config")
class HarmonizationConfigController(
    private val exportCodingFailedUseCase: ExportCodingFailedUseCase,
    private val importAllCodingFailedUseCase: ImportAllCodingFailedUseCase,
) {

    @Operation(summary = "Produces a .csv file containing the codings that are in the custom storage")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = ".csv file with one line of headers, comma-separated and always quoted",
                content = [Content(schema = Schema(implementation = String::class))],
            ),
        ],
    )
    @GetMapping("/coding/export", produces = ["text/csv"])
    fun export(
        @Parameter(description = "When true, will not only return the unmapped entries, but also the valid manual mappings.")
        @RequestParam("all")
        all: Boolean = false,
    ): ResponseEntity<String> =
        ResponseEntity
            .status(HttpStatus.OK)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("export.csv").build().toString(),
            )
            .body(exportCodingFailedUseCase(all).toCsv().orThrow())

    @Operation(summary = "Imports the contents of the CSV file, applying all approved mappings with a valid OMOP id.")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Import was successful, returns a JSON result object with information what was imported exactly",
                content = [Content(schema = Schema(implementation = ImportResponse.ImportSuccess::class))],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = ".csv file contains unexpected values",
                content = [Content(schema = Schema(implementation = ImportResponse.BadRequest::class))],
            ),
        ],
    )
    @PostMapping(
        "/coding/import",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun import(
        @DocRequestBody(description = ".csv file with one line of headers, created using 'Save As' from Usagi")
        @RequestBody
        file: MultipartFile,
    ): ResponseEntity<ImportResponse> =
        catch {
            InputStreamReader(file.inputStream)
                .use {
                    CsvToBeanBuilder<ImportItem>(CSVReaderBuilder(it).build())
                        .withType(ImportItem::class.java)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build()
                        .parse()
                }
        }
            .mapError { ImportResponse.BadRequest("CSV processing failed. Check the validity of the CSV file") }
            .map { ImportResponse.ImportSuccess(importAllCodingFailedUseCase(it)) }
            .merge().asEntity()

    private fun List<CodingFailed>.toCsv(): Result<String, Throwable> = catch {
        StringWriter().use { writer ->
            val csvWriter = CSVWriter(writer)
            csvWriter.writeNext(
                arrayOf(
                    CsvExportHeaders.CODE,
                    CsvExportHeaders.DISPLAY,
                    CsvExportHeaders.OCCURRENCE,
                    CsvExportHeaders.SYSTEM,
                    CsvExportHeaders.CONCEPT_ID,
                ),
            )
            this.forEach {
                csvWriter.writeNext(
                    arrayOf(
                        it.code,
                        it.display,
                        it.occurrence.toString(),
                        it.system,
                        it.conceptId?.toString(),
                    ),
                )
            }
            writer.toString()
        }
    }

    sealed class ImportResponse : ApiResponse {
        data class ImportSuccess(
            val importResult: ImportResult,
        ) : ImportResponse()

        data class BadRequest(
            val message: String,
        ) : ImportResponse() {
            override val status = HttpStatus.BAD_REQUEST
        }
    }
}

@NoArg
data class ImportItem(
    @CsvBindByName(column = CsvUsagiFileHeaders.SOURCE_SYSTEM, required = true)
    val system: String,
    @CsvBindByName(column = CsvUsagiFileHeaders.SOURCE_CODE, required = true)
    val code: String,
    @CsvBindByName(column = CsvUsagiFileHeaders.SOURCE_NAME)
    val display: String?,
    @CsvBindByName(column = CsvUsagiFileHeaders.CONCEPT_ID)
    val targetConceptId: Int?,
    @CsvBindByName(column = CsvUsagiFileHeaders.MAPPING_STATUS, required = true)
    val mappingStatus: String?,
)

/**
 * NoArgExtension
 * Alter config in gradle file when changing the fully qualified name of this annotation (moving)
 */
annotation class NoArg

/**
 * These are the column names by Usagi's saveAs export option
 */
@Suppress("unused")
private object CsvUsagiFileHeaders {
    const val SOURCE_CODE = "sourceCode"
    const val SOURCE_NAME = "sourceName"
    const val SOURCE_FREQUENCY = "sourceFrequency"
    const val SOURCE_AUTO_ASSIGNED_CONCEPT_IDS = "sourceAutoAssignedConceptIds"
    const val SOURCE_SYSTEM = "ADD_INFO:source_system"
    const val MATCH_SCORE = "matchScore"
    const val MAPPING_STATUS = "mappingStatus"
    const val EQUIVALENCE = "equivalence"
    const val STATUS_SET_BY = "statusSetBy"
    const val STATUS_SET_ON = "statusSetOn"
    const val CONCEPT_ID = "conceptId"
    const val CONCEPT_NAME = "conceptName"
    const val MAPPING_TYPE = "mappingType"
    const val COMMENT = "comment"
    const val CREATED_BY = "createdBy"
    const val CREATED_ON = "createdOn"
    const val ASSIGNED_REVIEWER = "assignedReviewer"
}

private object CsvExportHeaders {
    const val SYSTEM = "source_system"
    const val CODE = "source_code"
    const val OCCURRENCE = "source_frequency"
    const val DISPLAY = "source_name"
    const val CONCEPT_ID = "target_concept_id"
}
