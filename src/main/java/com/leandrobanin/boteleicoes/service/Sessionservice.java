package com.leandrobanin.boteleicoes.service;

import com.leandrobanin.boteleicoes.model.Usersession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia as sessões dos usuários em memória.
 * Limpa sessões expiradas automaticamente a cada 5 minutos.
 */
@Slf4j
@Service
public class Sessionservice {

    @Value("${bot.session-ttl-minutes:10}")
    private int sessionTtlMinutes;

    // Map: número de telefone → sessão
    private final ConcurrentHashMap<String, Usersession> sessions = new ConcurrentHashMap<>();

    public Usersession getOrCreate(String phoneNumber) {
        return sessions.computeIfAbsent(phoneNumber, Usersession::new);
    }

    public void update(Usersession session) {
        session.touch();
        sessions.put(session.getPhoneNumber(), session);
    }

    public void resetSession(String phoneNumber) {
        Usersession session = getOrCreate(phoneNumber);
        session.setState(Usersession.State.IDLE);
        session.setLastPolitician(null);
        update(session);
    }

    // Limpa sessões expiradas a cada 5 minutos
    @Scheduled(fixedDelay = 300_000)
    public void cleanExpiredSessions() {
        int before = sessions.size();
        sessions.entrySet().removeIf(e -> e.getValue().isExpired(sessionTtlMinutes));
        int removed = before - sessions.size();
        if (removed > 0) {
            log.debug("Sessões expiradas removidas: {}", removed);
        }
    }
}

