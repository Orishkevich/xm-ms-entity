package com.icthh.xm.ms.entity.config;

import io.github.jhipster.async.ExceptionHandlingAsyncTaskExecutor;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class AsyncConfiguration implements AsyncConfigurer {

    private final JHipsterProperties jHipsterProperties;

    public AsyncConfiguration(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    @Override
    @Bean(name = "taskScheduler")
    public Executor getAsyncExecutor() {
        log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(jHipsterProperties.getAsync().getCorePoolSize());
        executor.setMaxPoolSize(jHipsterProperties.getAsync().getMaxPoolSize());
        executor.setQueueCapacity(jHipsterProperties.getAsync().getQueueCapacity());
        executor.setThreadNamePrefix("entity-Executor-");
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
