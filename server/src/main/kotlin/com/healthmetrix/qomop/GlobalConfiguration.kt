package com.healthmetrix.qomop

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.security.SecureRandom

@Configuration
class GlobalConfiguration {

    @Bean
    fun provideTaskExecutor(): TaskExecutor = ThreadPoolTaskExecutor().apply {
        setQueueCapacity(0)
        setWaitForTasksToCompleteOnShutdown(true)
        setAwaitTerminationSeconds(60)
        initialize()
    }

    @Bean("applicationEventMulticaster")
    fun provideApplicationEventMulticaster(
        taskExecutor: TaskExecutor,
    ): ApplicationEventMulticaster = SimpleApplicationEventMulticaster().apply {
        setTaskExecutor(taskExecutor)
    }

    @Bean
    fun provideRandom(): SecureRandom = SecureRandom()
}
