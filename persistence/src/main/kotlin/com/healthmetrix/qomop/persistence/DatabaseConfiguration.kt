package com.healthmetrix.qomop.persistence

import com.healthmetrix.qomop.commons.Secrets
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "primaryEntityManager",
    transactionManagerRef = "primaryTransactionManager",
    basePackages = ["com.healthmetrix.qomop.persistence"],
)
class DatabaseConfiguration {

    // auto configuration disabled to allow default profile with no jpa
    @Bean("primaryDatasourceProperties")
    @Primary
    @ConfigurationProperties("spring.datasource-primary")
    fun provideDataSourceProperties() = DataSourceProperties()

    @Bean("primaryDatasource")
    @Profile("!rds-url-from-properties")
    @Primary
    fun providePostgresDataSource(
        @Qualifier("primaryDatasourceProperties")
        dataSourceProperties: DataSourceProperties,
        @Value("\${secrets.rds-primary-credentials}")
        credentialsLocation: String,
        secrets: Secrets,
    ): DataSource = TransactionAwareDataSourceProxy(
        dataSourceProperties.apply {
            url = "jdbc:${secrets[credentialsLocation]}"
        }.initializeDataSourceBuilder().build(),
    )

    @Bean("primaryDatasource")
    @Profile("rds-url-from-properties")
    @Primary
    fun providePostgresDataSourceFromProperties(
        @Qualifier("primaryDatasourceProperties")
        dataSourceProperties: DataSourceProperties,
    ): DataSource = TransactionAwareDataSourceProxy(
        dataSourceProperties.apply {
            url = "jdbc:${dataSourceProperties.url}"
        }.initializeDataSourceBuilder().build(),
    )

    @Bean("primaryEntityManager")
    @Primary
    fun primaryEntityManager(
        builder: EntityManagerFactoryBuilder,
        @Qualifier("primaryDatasource")
        dataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean = builder
        .dataSource(dataSource)
        .packages("com.healthmetrix.qomop.persistence")
        .persistenceUnit("datasource-primary")
        .build()

    @Bean("primaryTransactionManager")
    @Primary
    fun primaryTransactionManager(
        @Qualifier("primaryEntityManager")
        entityManagerFactoryBean: EntityManagerFactory,
    ): JpaTransactionManager = JpaTransactionManager(entityManagerFactoryBean)
}
