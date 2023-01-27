import com.healthmetrix.qomop.buildlogic.conventions.excludeReflect

plugins {
    id("com.healthmetrix.kotlin.conventions")
}

dependencies {
    implementation(projects.commons)
    api(libs.json)
    implementation(libs.jackson.jsr310)
    api(libs.jackson.kotlin) { excludeReflect() }
}
