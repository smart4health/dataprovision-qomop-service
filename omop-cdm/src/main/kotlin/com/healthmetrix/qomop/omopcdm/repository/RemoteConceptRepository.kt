package com.healthmetrix.qomop.omopcdm.repository

import com.healthmetrix.qomop.omopcdm.api.Concept
import com.healthmetrix.qomop.omopcdm.api.ConceptRepository
import com.healthmetrix.qomop.omopcdm.entity.ConceptEntity
import com.healthmetrix.qomop.omopcdm.entity.toConcept
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component

@Component
class RemoteConceptRepository(
    private val conceptRepository: ConceptCrudRepository,
) : ConceptRepository {

    override fun findById(conceptId: Int): Concept? = conceptRepository.findById(conceptId).orElse(null)?.toConcept()

    override fun findByName(conceptName: String): List<Concept> =
        conceptRepository.findByNameContainingIgnoreCase(conceptName).map { it.toConcept() }

    override fun findByConceptCode(conceptCode: String): List<Concept> =
        conceptRepository.findByConceptCode(conceptCode).map { it.toConcept() }

    override fun findAncestryRelated(conceptId: Int, distance: Int): List<Concept> =
        conceptRepository.findAncestryRelated(conceptId, 1, distance + 1).map { it.toConcept() }
}

interface ConceptCrudRepository : CrudRepository<ConceptEntity, Int> {

    @Query(
        nativeQuery = true,
        value = "SELECT DISTINCT *\n" +
            "FROM\n" +
            "    (SELECT c.concept_id,\n" +
            "                  c.concept_name,\n" +
            "                  c.standard_concept,\n" +
            "                  c.invalid_reason,\n" +
            "                  c.concept_code,\n" +
            "                  c.concept_class_id,\n" +
            "                  c.domain_id,\n" +
            "                  c.vocabulary_id,\n" +
            "                  c.valid_start_date,\n" +
            "                  c.valid_end_date,\n" +
            "                  'Has ancestor of',\n" +
            "                  min_levels_of_separation RELATIONSHIP_DISTANCE\n" +
            "     FROM concept_ancestor ca\n" +
            "     JOIN concept c ON c.concept_id = ca.ancestor_concept_id\n" +
            "     WHERE descendant_concept_id = :conceptId\n" +
            "         AND ancestor_concept_id <> :conceptId\n" +
            "     UNION SELECT c.concept_id,\n" +
            "                  c.concept_name,\n" +
            "                  c.standard_concept,\n" +
            "                  c.invalid_reason,\n" +
            "                  c.concept_code,\n" +
            "                  c.concept_class_id,\n" +
            "                  c.domain_id,\n" +
            "                  c.vocabulary_id,\n" +
            "                  c.valid_start_date,\n" +
            "                  c.valid_end_date,\n" +
            "                  'Has descendant of',\n" +
            "                  min_levels_of_separation RELATIONSHIP_DISTANCE\n" +
            "     FROM concept_ancestor ca\n" +
            "     JOIN concept c ON c.concept_id = ca.descendant_concept_id\n" +
            "     WHERE ancestor_concept_id = :conceptId\n" +
            "         AND descendant_concept_id <> :conceptId\n" +
            "     UNION SELECT DISTINCT c3.concept_id,\n" +
            "                           c3.concept_name,\n" +
            "                           c3.standard_concept,\n" +
            "                           c3.invalid_reason,\n" +
            "                           c3.concept_code,\n" +
            "                           c3.concept_class_id,\n" +
            "                           c3.domain_id,\n" +
            "                           c3.vocabulary_id,\n" +
            "                           c3.valid_start_date,\n" +
            "                           c3.valid_end_date,\n" +
            "                           CONCAT('Has relation to descendant of : ', relationship_name) RELATIONSHIP_NAME,\n" +
            "                           min_levels_of_separation RELATIONSHIP_DISTANCE\n" +
            "     FROM\n" +
            "         (SELECT *\n" +
            "          FROM concept\n" +
            "          WHERE concept_id = :conceptId) c1\n" +
            "     JOIN concept_ancestor ca1 ON c1.concept_id = ca1.ancestor_concept_id\n" +
            "     JOIN concept_relationship cr1 ON ca1.descendant_concept_id = cr1.concept_id_2\n" +
            "     AND cr1.relationship_id = 'Maps to'\n" +
            "     AND cr1.invalid_reason IS NULL\n" +
            "     JOIN relationship r ON r.relationship_id = cr1.relationship_id\n" +
            "     JOIN concept c3 ON cr1.concept_id_1 = c3.concept_id) ALL_RELATED\n" +
            "WHERE relationship_distance < :maxDistance and relationship_distance >= :minDistance " +
            "ORDER BY relationship_distance ASC",
    )
    fun findAncestryRelated(
        @Param("conceptId") conceptId: Int,
        @Param("minDistance") minDistance: Int,
        @Param("maxDistance") maxDistance: Int,
    ): List<ConceptEntity>

    fun findByNameContainingIgnoreCase(conceptName: String): List<ConceptEntity>

    fun findByConceptCode(conceptCode: String): List<ConceptEntity>
}
