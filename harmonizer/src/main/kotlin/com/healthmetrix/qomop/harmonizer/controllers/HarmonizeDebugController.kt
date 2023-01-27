package com.healthmetrix.qomop.harmonizer.controllers

import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizeByAncestryUseCase
import com.healthmetrix.qomop.harmonizer.harmonize.HarmonizeByRelationUseCase
import com.healthmetrix.qomop.omopcdm.api.Concept
import com.healthmetrix.qomop.omopcdm.api.ConceptRepository
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/harmonization-debug")
@Profile("debug")
@Hidden
class HarmonizeDebugController(
    private val conceptRepository: ConceptRepository,
    private val conceptByRelationUseCase: HarmonizeByRelationUseCase,
    private val conceptByAncestryUseCase: HarmonizeByAncestryUseCase,

) {

    @GetMapping("/concept/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getConceptById(
        @PathVariable("id") id: Int,
    ): Concept? = conceptRepository.findById(id)

    @GetMapping("/concept/{id}/byRelation", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun conceptByRelation(
        @PathVariable("id") id: Int,
    ): Concept? = conceptRepository.findById(id)?.let { conceptByRelationUseCase(it) }

    @GetMapping("/concept/{id}/byAncestry", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun conceptByAncestry(
        @PathVariable("id") id: Int,
    ): Concept? = conceptRepository.findById(id)?.let { conceptByAncestryUseCase(it) }
}
