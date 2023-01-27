import com.healthmetrix.qomop.buildlogic.conventions.excludeReflect

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.healthmetrix.kotlin.conventions")
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(projects.commons)
    implementation(projects.omopCdm.omopApi)

    implementation(libs.spring.framework.web)
    implementation(libs.spring.framework.context)

    implementation(libs.springdoc.openapi) { excludeReflect() }
    implementation(libs.springdoc.ui)
}
