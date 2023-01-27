package com.healthmetrix.qomop.commons

enum class SupportedVocabulary(
    val omopVocabularyId: String,
    val fhirSystemUri: String,
    val preferred: Boolean,
) {
    SNOMED("SNOMED", "http://snomed.info/sct", true),
    CPT4("CPT4", "http://www.ama-assn.org/go/cpt", false),
    HCPCS("HCPCS", "http://purl.bioontology.org/ontology/HCPCS", false),
    LOINC("LOINC", "http://loinc.org", true),
    RXNORM("RxNorm", "http://www.nlm.nih.gov/research/umls/rxnorm", true),
    UCUM("UCUM", "http://unitsofmeasure.org", false),
    NDC("NDC", "http://hl7.org/fhir/sid/ndc", false),
    CAP("CAP", "http://cap.org/protocols", false),
    NCIT("NCIt", "http://ncimeta.nci.nih.gov", false),
    ICD9CM("ICD9CM", "http://hl7.org/fhir/sid/icd-9-cm", false),
    ICD9Proc("ICD9Proc", "http://hl7.org/fhir/sid/icd-9-cm/procedure", false),
    ICD10("ICD10", "http://hl7.org/fhir/sid/icd-10", false),
    ICD10PCS("ICD10PCS", "http://hl7.org/fhir/sid/ex-icd-10-procedures", false),
    ICD10NL("ICD10NL", "http://hl7.org/fhir/sid/icd-10-nl", false),
    ICD10GM("ICD10GM", "http://fhir.de/CodeSystem/dimdi/icd-10-gm", false),
    ICD10CM("ICD10CM", "http://hl7.org/fhir/sid/icd-10-cm", false),
    ICDO3("ICDO3", "http://terminology.hl7.org/CodeSystem/icd-o-3", false),
    ATC("ATC", "http://www.whocc.no/atc", false),
    MEDRT("MEDRT", "http://hl7.org/fhir/medrt", false),
    NDFRT("NDFRT", "http://hl7.org/fhir/ndfrt", false),
    CVX("CVX", "http://hl7.org/fhir/sid/cvx", false),
    CLINVAR("ClinVar", "http://www.ncbi.nlm.nih.gov/clinvar", false),
    NUCC("NUCC", "http://nucc.org/provider-taxonomy", false),
    CDT("CDT", "http://ada.org/cdt", false),
    HGNC("HGNC", "http://www.genenames.org", false),
}

fun String.asFhirSystemUri(): SupportedVocabulary? =
    SupportedVocabulary.values().firstOrNull { it.fhirSystemUri == this }

fun String.asOmopVocabularyId(): SupportedVocabulary? =
    SupportedVocabulary.values().firstOrNull { it.omopVocabularyId == this }
