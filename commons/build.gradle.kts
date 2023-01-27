import com.healthmetrix.qomop.buildlogic.conventions.excludeReflect

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.healthmetrix.kotlin.conventions")
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(libs.kotlin.reflect)

    api(libs.slf4j.api)
    api(libs.logback.encoder)
    api(libs.result)

    implementation(libs.spring.framework.web)
    implementation(libs.spring.framework.context)
    implementation(libs.aws.secretsmanager)
    implementation(libs.spring.cloud.vault.config)

    api(libs.jackson.kotlin) { excludeReflect() }
    api(libs.micrometer.prometheus)
}
