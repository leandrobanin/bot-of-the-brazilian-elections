package com.leandrobanin.boteleicoes.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache em memória para respostas da IA.
 * Evita chamar a API toda vez que o mesmo político é consultado.
 *
 * Caches disponíveis:
 *   - resumo-politico  → card resumo (5 pontos)
 *   - detalhes-politico → card detalhado (carreira/propostas/polêmicas)
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("resumo-politico", "detalhes-politico");
    }
}
