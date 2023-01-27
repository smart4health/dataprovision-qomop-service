package com.healthmetrix.qomop

import org.springframework.boot.actuate.autoconfigure.metrics.data.RepositoryMetricsAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Exclusions:
 * - DataSourceAutoConfiguration: since we have two data sources:
 *      com.healthmetrix.qomop.persistence.DatabaseConfiguration
 *      com.healthmetrix.qomop.omopcdm.OmopDatabaseConfiguration
 * - RepositoryMetricsAutoConfiguration: we don't need crud repositories being auto timed for now
 */
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, RepositoryMetricsAutoConfiguration::class])
@ConfigurationPropertiesScan
class QomopApplication

fun main(args: Array<String>) {
    runApplication<QomopApplication>(*args)
}
