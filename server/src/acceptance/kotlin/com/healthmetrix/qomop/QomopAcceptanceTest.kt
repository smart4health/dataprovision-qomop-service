package com.healthmetrix.qomop

import com.healthmetrix.qomop.commons.logger
import com.healthmetrix.qomop.commons.test.TestUtils
import com.healthmetrix.qomop.commons.test.json
import com.healthmetrix.qomop.harmonizer.controllers.HarmonizationController
import com.healthmetrix.qomop.harmonizer.harmonize.Coding
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailed
import com.healthmetrix.qomop.persistence.harmonization.api.CodingFailedRepository
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID
import kotlin.system.measureTimeMillis

@Suppress("FunctionName")
@SpringBootTest(
    classes = [QomopApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.cloud.vault.enabled=false"],
)
@AutoConfigureMockMvc
@ActiveProfiles("acceptance")
class QomopAcceptanceTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val codingFailedRepository: CodingFailedRepository,
) {
    private val testUtils = TestUtils()

    private val input: CodingsJson = testUtils.fromJsonFile("harmonization_input.json")
    private val expected: CodingsJson = testUtils.fromJsonFile("harmonization_expected.json")

    @Test
    fun `health check returns UP`() {
        mockMvc.get("/actuator/health")
            .andExpect {
                jsonPath("$.status", `is`("UP"))
            }
    }

    @Test
    fun `test harmonizer`() {
        mockMvc.post("/v1/harmonizer/coding") {
            contentType = MediaType.APPLICATION_JSON
            content = json {
                "system" to "http://www.whocc.no/atc"
                "code" to "N02BE01"
            }
        }
            .andExpect { status { isOk() } }
            .andExpect {
                content {
                    string("""{"success":false,"message":"SuccessOnRelation, harmonized=true, standardized=true","harmonizedCoding":{"system":"http://www.nlm.nih.gov/research/umls/rxnorm","code":"161","display":"acetaminophen"},"harmonized":true,"standardized":true}""")
                }
            }
    }

    /**
     * Tests all HarmonizationResults (see console out)
     * - SuccessAlreadyHarmonized
     * - SuccessOnRelation
     * - SuccessOnLocalMapping
     * - FailureCodeUnknown
     * - FailureSystemUnmappable
     * - SuccessOnAncestry
     * - FailureInvalidCoding
     * - FailureGivingUp
     */
    @Test
    fun `test harmonizer with for SuccessOnRelation and FailureSystemUnmappable batch number of codings`() {
        val now = Instant.now()
        // adds one valid mapping to concept 3028553
        codingFailedRepository.save(
            vitalSignsCodingFailedWithValidAt(
                validStart = now.minusSeconds(100),
                validEnd = now.plusSeconds(100),
                conceptId = 3028553,
            ),
        )
        // adds one invalid/outdated mapping that should be ignored to 123456
        codingFailedRepository.save(
            vitalSignsCodingFailedWithValidAt(
                validStart = now.minusSeconds(100),
                validEnd = now.minusSeconds(100),
                conceptId = 123456,
            ),
        )

        input.coding.forEachIndexed { index, coding ->
            val responseBodyAsString = mockMvc.post("/v1/harmonizer/coding") {
                contentType = MediaType.APPLICATION_JSON
                content = testUtils.toJsonString(coding)
            }
                .andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString

            val result = testUtils
                .fromJsonString<HarmonizationController.HarmonizationResponse.HarmonizationDone>(responseBodyAsString)

            logger.info(result.message)
            assertThat(result.harmonizedCoding ?: coding).isEqualTo(expected.coding[index])
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 10])
    fun `test tracing for unsupported categories`(id: Int) {
        mockMvc.post("/v1/tracer/data-category/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = json {
                "system" to "http://www.whocc.no/atc"
                "code" to "N02BE01"
            }
        }
            .andExpect { status { isBadRequest() } }
    }

    @ParameterizedTest
    @ValueSource(ints = [5, 6, 7, 8, 9])
    fun `test tracings for enabled categories`(id: Int) {
        mockMvc.post("/v1/tracer/data-category/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = json {
                "system" to "http://www.whocc.no/atc"
                "code" to "N02BE01"
            }
        }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `trace surgical procedure down 3 paths successfully`() {
        mockMvc.post("/v1/tracer/data-category/5") {
            contentType = MediaType.APPLICATION_JSON
            content = json {
                "system" to "http://snomed.info/sct"
                "code" to "82046009"
            }
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.match", `is`(true)) }
    }

    @Test
    fun `trace surgical procedure down 12 paths unsuccessfully and again for caching`() {
        val firstRunMillis = measureTimeMillis {
            mockMvc.post("/v1/tracer/data-category/5") {
                contentType = MediaType.APPLICATION_JSON
                content = json {
                    "system" to "http://snomed.info/sct"
                    "code" to "257272008"
                }
            }
                .andExpect { status { isOk() } }
                .andExpect { jsonPath("$.match", `is`(false)) }
        }

        val secondRunMillis = measureTimeMillis {
            mockMvc.post("/v1/tracer/data-category/5") {
                contentType = MediaType.APPLICATION_JSON
                content = json {
                    "system" to "http://snomed.info/sct"
                    "code" to "257272008"
                }
            }
                .andExpect { status { isOk() } }
                .andExpect { jsonPath("$.match", `is`(false)) }
        }

        assertThat(firstRunMillis).isGreaterThan(1000)
        assertThat(secondRunMillis).isLessThan(200)
    }

    @Test
    fun `trace surgical procedure with itself`() {
        mockMvc.post("/v1/tracer/data-category/5") {
            contentType = MediaType.APPLICATION_JSON
            content = json {
                "system" to "http://snomed.info/sct"
                "code" to "387713003"
            }
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.match", `is`(true)) }
    }

    @Test
    fun `trace item unknown to omop`() {
        mockMvc.post("/v1/tracer/data-category/5") {
            contentType = MediaType.APPLICATION_JSON
            content = json {
                "system" to "hello"
                "code" to "goodbye"
            }
        }
            .andExpect { status { isOk() } }
            .andExpect { jsonPath("$.match", `is`(false)) }
    }

    private data class CodingsJson(
        val coding: List<Coding>,
    )

    private fun vitalSignsCodingFailedWithValidAt(
        validStart: Instant,
        validEnd: Instant,
        conceptId: Int,
    ) = CodingFailed(
        id = UUID.randomUUID(),
        system = "http://terminology.hl7.org/CodeSystem/observation-category",
        code = "vital-signs",
        display = "Vital Signs",
        conceptId = conceptId, // LOINC code for vital signs
        occurrence = 2,
        validStart = validStart,
        validEnd = validEnd,
    )
}
