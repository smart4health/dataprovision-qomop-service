package com.healthmetrix.qomop

import com.healthmetrix.qomop.commons.DocumentationConstants
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DocumentationConfig {

    @Bean
    fun api(
        documentationInfo: DocumentationInfo,
    ): OpenAPI = OpenAPI()
        .info(documentationInfo.toApiInfo())
        .addTagsItem(codingExchangeApiTag)
        .addTagsItem(harmonizationTag)
        .addTagsItem(codingTracingTag)
        .components(
            Components()
                .addSecuritySchemes(
                    DocumentationConstants.BASIC_AUTH,
                    basicAuthScheme,
                ),
        )
        .externalDocs(
            ExternalDocumentation().apply {
                url = "https://github.com/smart4health/dataprovision-deident-service/tree/main/docs"
                description = "Detailed Guide to the Coding Import / Export API"
            },
        )

    private val basicAuthScheme = SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("basic")
        .description("Basic auth credentials")
    private val codingExchangeApiTag = Tag()
        .name(DocumentationConstants.CODING_EXCHANGE_API_TAG)
        .description("Import and export custom FHIR Coding mappings")
    private val harmonizationTag = Tag()
        .name(DocumentationConstants.HARMONIZATION_API_TAG)
        .description("Run Harmonization queries for electronic health resources")
    private val codingTracingTag = Tag()
        .name(DocumentationConstants.CONCEPT_TRACING_API_TAG)
        .description("Run Tracing queries for FHIR Coding / OMOP Concept items based on data categories")
}

@ConfigurationProperties(prefix = "documentation-info")
data class DocumentationInfo(
    val title: String,
    val description: String,
    val contact: ContactConfig,
) {
    data class ContactConfig(
        val name: String,
        val email: String,
    )

    fun toApiInfo(): Info {
        return Info()
            .title(title)
            .description(description)
            .contact(
                Contact()
                    .name(contact.name)
                    .email(contact.email),
            )
    }
}
