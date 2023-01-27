import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // version is determined by the implementation dependency of build-logic
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += listOf("-Xjsr305=strict")
        // TODO: remove once KGP 1.8 is released or this is closed:
        //  https://youtrack.jetbrains.com/issue/KT-54116/Add-JVM-target-bytecode-version-19
        kotlinOptions.jvmTarget = "17"
    }

    withType<Test> {
        useJUnitPlatform()
    }
}
