package com.healthmetrix.qomop.omopcdm

import com.healthmetrix.qomop.commons.Secrets
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "omopEntityManager",
    transactionManagerRef = "omopTransactionManager",
    basePackages = ["com.healthmetrix.qomop.omopcdm"],
)
class OmopDatabaseConfiguration {

    // auto configuration disabled to allow default profile with no jpa
    @Bean("omopDatasourceProperties")
    @ConfigurationProperties("spring.datasource-omop")
    fun provideOmopDataSourceProperties() = DataSourceProperties()

    @Bean("omopDatasource")
    @Profile("!secrets-vault")
    fun providePostgresDataSource(
        @Qualifier("omopDatasourceProperties")
        dataSourceProperties: DataSourceProperties,
        @Value("\${secrets.rds-omop-credentials}")
        credentialsLocation: String,
        secrets: Secrets,
    ): DataSource = TransactionAwareDataSourceProxy(
        dataSourceProperties.apply {
            url = "jdbc:${secrets[credentialsLocation]}&currentSchema=cdm"
        }.initializeDataSourceBuilder().build(),
    )

    @Bean("omopDatasource")
    @Profile("secrets-vault")
    fun providePostgresDataSourceFromProperties(
        @Qualifier("omopDatasourceProperties")
        dataSourceProperties: DataSourceProperties,
    ): DataSource = TransactionAwareDataSourceProxy(
        dataSourceProperties.apply {
            url = "jdbc:${dataSourceProperties.url}&currentSchema=cdm"
        }.initializeDataSourceBuilder().build(),
    )

    @Bean("omopEntityManager")
    fun omopEntityManager(
        builder: EntityManagerFactoryBuilder,
        @Qualifier("omopDatasource")
        dataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean = builder
        .dataSource(dataSource)
        .packages("com.healthmetrix.qomop.omopcdm")
        .persistenceUnit("datasource-omop")
        .build()

    @Bean("omopTransactionManager")
    fun omopTransactionManager(
        @Qualifier("omopEntityManager")
        entityManagerFactoryBean: EntityManagerFactory,
    ): JpaTransactionManager = JpaTransactionManager(entityManagerFactoryBean)
}
