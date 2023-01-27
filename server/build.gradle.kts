@file:Suppress("UnstableApiUsage")

import com.healthmetrix.qomop.buildlogic.conventions.excludeReflect
import com.healthmetrix.qomop.buildlogic.conventions.exclusionsSpringTestImplementation
import com.healthmetrix.qomop.buildlogic.conventions.exclusionsSpringTestRuntime
import com.healthmetrix.qomop.buildlogic.conventions.registeringExtended
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.healthmetrix.kotlin.conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

tasks.withType<BootBuildImage> {
    imageName.set("healthmetrixgmbh/qomop")
}

dependencies {
    implementation(projects.commons)
    runtimeOnly(projects.harmonizer)
    runtimeOnly(projects.tracer)
    runtimeOnly(projects.persistence)
    runtimeOnly(projects.omopCdm)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)

    // metrics
    implementation(libs.micrometer.cloudwatch2)

    implementation(libs.springdoc.openapi) { excludeReflect() }
    implementation(libs.springdoc.ui)

    testImplementation(projects.commonsTest)
    testImplementation(projects.persistence.harmonizationApi)
    testImplementation(projects.harmonizer)
    testImplementation(libs.bundles.test.spring.implementation) { exclusionsSpringTestImplementation() }
    testRuntimeOnly(libs.bundles.test.spring.runtime) { exclusionsSpringTestRuntime() }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class)
        val acceptance by registeringExtended(test, libs.versions.junit.get()) {}
    }
}
