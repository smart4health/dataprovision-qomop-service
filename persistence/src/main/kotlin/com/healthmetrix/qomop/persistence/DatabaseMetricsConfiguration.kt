package com.healthmetrix.qomop.persistence

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.db.DatabaseTableMetrics
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * Metrics name = db.table.size
 * Only using primary database since omop cdm is only used with read access, hence no changes
 */
@Configuration
class DatabaseMetricsConfiguration(
    private val registry: MeterRegistry,
    @Qualifier("primaryDatasource")
    private val dataSource: DataSource,
) {

    @PostConstruct
    fun initializeTableSizeMetrics() {
        DatabaseTableMetrics.monitor(registry, "coding_failed", "qomop", dataSource)
    }
}
