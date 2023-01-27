@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

rootProject.name = "qomop-service"
include("persistence")
include("persistence:harmonization-api")
include("omop-cdm")
include("omop-cdm:omop-api")
include("commons")
include("server")
include("harmonizer")
include("tracer")
include("commons-test")
