package com.healthmetrix.qomop.tracer

/**
 * Categories defined in the context of the User Consent
 */
enum class DataSelectionCategory(
    val id: Int,
) {

    /**
     * Tracing takes place on client side already by comparing:
     * ResourceType Medication or MedicationStatement
     */
    MEDICATIONS(1),

    /**
     * Traceable on client side already by:
     * ResourceType AllergyIntolerance
     */
    ALLERGIES_INTOLERANCES(2),

    /**
     * Traceable on client side already by:
     * ResourceType Condition
     */
    CLINICAL_CONDITIONS(3),

    /**
     * Traceable on client side already by:
     * ResourceType Immunization
     */
    IMMUNIZATIONS(4),

    /**
     * FHIR Resource path Procedure.code is sent as Coding(s)
     */
    PROCEDURES_SURGERIES(5),

    /**
     * FHIR Resource path Device.type is sent as Coding(s)
     */
    MEDICAL_DEVICES_IMPLANTS(6),

    /**
     * FHIR Resource path Observation.code is sent as Coding(s)
     */
    VITAL_SIGNS(7),

    /**
     * This Type has two indicators:
     * FHIR Resource path Observation.code is sent as Coding(s) and traced in this service
     * FHIR Resource path Observation.category=laboratory is traced on the client side
     */
    DIAGNOSTIC_RESULTS_LABORATORY(8),

    /**
     * FHIR Resource path Observation.code is sent as Coding(s) and traced in this service
     * FHIR Resource path Observation.category = activity is traced on the client side
     */
    DIAGNOSTIC_RESULTS_FITNESS(9),

    /**
     * Traceable on client side already by:
     * ResourceType Questionnaire/Response
     */
    QUESTIONNAIRES(10),
    ;

    companion object {
        fun fromId(value: Int) = values().firstOrNull { it.id == value }
    }

    override fun toString(): String {
        return "$name ($id)"
    }
}
