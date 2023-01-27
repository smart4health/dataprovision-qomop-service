package com.healthmetrix.qomop.commons

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

interface Secrets {
    operator fun get(key: String): String // missing secrets are fatal
}

@Component
@Profile("secrets-aws")
internal class AwsSecrets(
    private val secretsManagerClient: SecretsManagerClient,
) : Secrets {

    private val cache = mutableMapOf<String, String>()

    override fun get(key: String): String {
        return cache[key] ?: run {
            GetSecretValueRequest.builder()
                .secretId(key)
                .build()
                .let(secretsManagerClient::getSecretValue)
                .secretString()
                .also { cache[key] = it }
        }
    }
}

@Configuration
@Profile("secrets-aws")
internal class SecretsConfiguration {
    @Bean
    fun provideSecretsManagerClient(): SecretsManagerClient =
        SecretsManagerClient.builder().build()
}

/**
 * Can be used locally to mock remote secret paths. See application.yaml:
 * recontact/dev/rds-credentials/recontact -> rds-credentials.recontact
 */
@Component
@Profile("!secrets-aws")
internal class MockSecrets(
    private val env: Environment,
) : Secrets {
    override fun get(key: String): String =
        (listOf("mock-secrets") + key.split("/").drop(2))
            .joinToString(".")
            .let(env::getProperty)!!
}
