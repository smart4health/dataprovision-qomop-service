import com.healthmetrix.qomop.buildlogic.conventions.excludeReflect
import com.healthmetrix.qomop.buildlogic.conventions.exclusionsSpringTestImplementation
import com.healthmetrix.qomop.buildlogic.conventions.exclusionsSpringTestRuntime

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.healthmetrix.kotlin.conventions")
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.noarg)
}

dependencies {
    implementation(projects.commons)
    implementation(projects.persistence.harmonizationApi)
    implementation(projects.omopCdm.omopApi)

    implementation(libs.spring.framework.web)
    implementation(libs.spring.framework.context)
    implementation(libs.spring.framework.tx)
    implementation(libs.opencsv)

    implementation(libs.springdoc.openapi) { excludeReflect() }
    implementation(libs.springdoc.ui)

    testImplementation(projects.commonsTest)
    testImplementation(libs.bundles.test.spring.implementation) { exclusionsSpringTestImplementation() }
    testRuntimeOnly(libs.bundles.test.spring.runtime) { exclusionsSpringTestRuntime() }
    testRuntimeOnly(projects.omopCdm)
    testRuntimeOnly(projects.persistence)
}

noArg {
    annotation("com.healthmetrix.qomop.harmonizer.controllers.NoArg")
}
