package com.leandrobanin.boteleicoes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configura o executor para processamento assíncrono das mensagens.
 * Garante que o webhook responda rápido enquanto a IA processa em paralelo.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "messageExecutor")
    public Executor messageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("bot-msg-");
        executor.initialize();
        return executor;
    }
}
