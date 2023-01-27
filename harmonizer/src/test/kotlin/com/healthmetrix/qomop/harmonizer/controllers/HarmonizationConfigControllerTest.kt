package com.healthmetrix.qomop.harmonizer.controllers

import com.healthmetrix.qomop.harmonizer.feedback.ExportCodingFailedUseCase
import com.healthmetrix.qomop.harmonizer.feedback.ImportAllCodingFailedUseCase
import com.healthmetrix.qomop.harmonizer.feedback.ImportResult
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import java.util.UUID

class HarmonizationConfigControllerTest {

    private val codingFailedId = UUID.randomUUID()

    private val exportCodingFailedUseCase: ExportCodingFailedUseCase = mockk()
    private val importAllCodingFailedUseCase: ImportAllCodingFailedUseCase = mockk()
    private val underTest =
        HarmonizationConfigController(
            exportCodingFailedUseCase,
            importAllCodingFailedUseCase,
        )

    @Nested
    inner class Export {
        @Test
        fun `export has content disposition attachment with filename set and csv content`() {
            every { exportCodingFailedUseCase() } returns listOf(
                CodingFailed(
                    codingFailedId,
                    "http://firefirefire",
                    "some-code",
                    "some-display-with-comma-,-",
                    1,
                    null,
                    null,
                    null,
                ),
            )
            underTest.export().let {
                assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
                assertThat(it.headers.contentDisposition.toString()).isEqualTo("""attachment; filename="export.csv"""")
                assertThat(it.body.toString()).isEqualTo(
                    """
                        "source_code","source_name","source_frequency","source_system","target_concept_id"
                        "some-code","some-display-with-comma-,-","1","http://firefirefire",
                        
                    """.trimIndent(),
                )
            }
        }

        @Test
        fun `export for empty list`() {
            every { exportCodingFailedUseCase() } returns emptyList()
            underTest.export().let {
                assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
                assertThat(it.body.toString()).isEqualTo(
                    """
                        "source_code","source_name","source_frequency","source_system","target_concept_id"
                        
                    """.trimIndent(),
                )
            }
        }
    }

    @Nested
    inner class Import {
        @Test
        fun `import for one valid item and one skipped`() {
            val validItem = ImportItem(
                "http://terminology.hl7.org/CodeSystem/observation-category",
                "vital-signs",
                "vital-signs",
                3028553,
                "LOINC",
            )
            val skippedItem = ImportItem("some system", "skipped code", "skipped display", null, null)
            val result = ImportResult(
                listOf(validItem),
                listOf(skippedItem),
                2,
            )
            every { importAllCodingFailedUseCase(any()) } returns result

            val file = MockMultipartFile(
                "mappings-to-upload.csv",
                javaClass.classLoader
                    .getResource("csv/mappings-to-upload.csv")!!
                    .readBytes(),
            )

            underTest.import(file).let {
                assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
                assertThat(it.body).isEqualTo(HarmonizationConfigController.ImportResponse.ImportSuccess(result))
            }
        }

        @Test
        fun `import for empty file`() {
            underTest.import(MockMultipartFile("some empty csv file", null)).let {
                assertThat(it.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            }
        }

        @Test
        fun `import for invalid csv file with unexpected columns`() {
            val file = MockMultipartFile(
                "invalid-headers.csv",
                javaClass.classLoader
                    .getResource("csv/invalid-headers.csv")!!
                    .readBytes(),
            )

            underTest.import(file).let {
                assertThat(it.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            }
        }
    }
}
