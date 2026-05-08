package com.leandrobanin.boteleicoes.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Guarda o estado da conversa de cada usuário.
 * Permite saber se o usuário está esperando por "saber mais"
 * ou se está iniciando uma nova consulta.
 */
@Data
public class Usersession {

    public enum State {
        IDLE,           // esperando qualquer mensagem
        AWAITING_MORE   // acabou de receber o resumo, pode pedir "mais"
    }   

    private String phoneNumber;
    private State state = State.IDLE;
    private String lastPolitician;       // nome do último político consultado
    private LocalDateTime lastActivity;

    public Usersession(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.lastActivity = LocalDateTime.now();
    }

    public void touch() {
        this.lastActivity = LocalDateTime.now();
    }

    public boolean isExpired(int ttlMinutes) {
        return lastActivity.plusMinutes(ttlMinutes).isBefore(LocalDateTime.now());
    }
}
