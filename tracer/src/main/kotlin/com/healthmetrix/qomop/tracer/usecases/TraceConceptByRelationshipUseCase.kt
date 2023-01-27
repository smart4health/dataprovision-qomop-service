package com.healthmetrix.qomop.tracer.usecases

import com.healthmetrix.qomop.omopcdm.api.ConceptRelationshipRepository
import org.springframework.stereotype.Component

/**
 * Given two OMOP Concept IDs and a RelationshipId, the use case tries to trace a route via the Relationship provided.
 * As the algorithm is exhaustive, and does not have a maximum depth, it should be used cautiously with "downward"
 * relationships that grow exponentially, e.g.:
 * - Good: relationshipId "Is a" (= belongs to the group concept X)
 * - Bad: "Subsumes" (is enclosing multiple concepts XYZ), the inverse of the one above.
 * A simple in memory cache is added since we can expect to see very similar concepts for patient data.
 */
@Component
class TraceConceptByRelationshipUseCase(
    private val conceptRelationshipRepository: ConceptRelationshipRepository,
) {

    val cache: HashMap<TracingQuery, Boolean> = hashMapOf()

    operator fun invoke(query: TracingQuery): Boolean =
        cache.getOrPut(query) { traceRecursively(query, depth = 0) }

    private fun traceRecursively(
        input: TracingQuery,
        visited: MutableSet<Int> = mutableSetOf(),
        depth: Int,
    ): Boolean {
        if (input.targetConceptId == input.queryConceptId) {
            return true
        }
        visited.add(input.queryConceptId)

        val results = conceptRelationshipRepository.findBySourceConceptId(input.queryConceptId)
            .filter { it.relationship.id == input.relationshipId }
            .map { it.targetConcept }
            .filter { result -> visited.find { it == result.id } == null }
            .toMutableSet()

        return if (results.find { it.id == input.targetConceptId } != null) {
            true
        } else {
            results.fold(false) { total, item ->
                total || traceRecursively(input.copy(queryConceptId = item.id), visited, depth + 1)
            }
        }
    }
}
