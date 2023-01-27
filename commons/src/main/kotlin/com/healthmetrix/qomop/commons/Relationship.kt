package com.healthmetrix.qomop.commons

enum class Relationship(val id: String) {
    IS_A("Is a"),
    SUBSUMES("Subsumes"),
    PANEL_CONTAINS("Panel contains"),
    CONTAINED_IN_PANEL("Contained in panel"),
    NON_STANDARD_TO_STANDARD_MAPPING("Maps to"),
}
