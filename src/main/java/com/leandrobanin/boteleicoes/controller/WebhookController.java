package com.leandrobanin.boteleicoes.controller;

import com.leandrobanin.boteleicoes.dto.EvolutionWebhookPayload;
import com.leandrobanin.boteleicoes.service.Messageprocessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Recebe os webhooks da Evolution API (mensagens do WhatsApp).
 *
 * Configure na Evolution API:
 *   URL: https://seu-dominio.com/webhook/whatsapp
 *   Eventos: MESSAGES_UPSERT
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final Messageprocessor messageProcessor;

    @Value("${bot.webhook-secret}")
    private String webhookSecret;

    /**
     * Endpoint principal do webhook.
     * A Evolution API vai chamar este endpoint quando chegar uma mensagem.
     */
    @PostMapping("/whatsapp")
    public ResponseEntity<Void> receiveMessage(
            @RequestBody EvolutionWebhookPayload payload,
            @RequestHeader(value = "apikey", required = false) String apiKey) {

        // Validação básica de segurança
        if (webhookSecret != null && !webhookSecret.equals(apiKey)) {
            log.warn("Requisição com apikey inválida bloqueada");
            return ResponseEntity.status(401).build();
        }

        // Só processa eventos de mensagens recebidas
        if (!"MESSAGES_UPSERT".equals(payload.getEvent())) {
            return ResponseEntity.ok().build();
        }

        var data = payload.getData();
        if (data == null || data.getKey() == null) {
            return ResponseEntity.ok().build();
        }

        // Ignora mensagens enviadas pelo próprio bot
        if (data.getKey().isFromMe()) {
            return ResponseEntity.ok().build();
        }

        // Ignora mensagens de grupos (JID termina com @g.us)
        String from = data.getKey().getRemoteJid();
        if (from.endsWith("@g.us")) {
            log.debug("Mensagem de grupo ignorada: {}", from);
            return ResponseEntity.ok().build();
        }

        // Extrai o texto da mensagem
        String text = extractText(data.getMessage());
        if (text == null || text.isBlank()) {
            return ResponseEntity.ok().build();
        }

        String userName = data.getPushName() != null ? data.getPushName() : "usuário";
        log.info("Nova mensagem de {} ({}): {}", userName, from, text);

        // Processa de forma assíncrona para responder rapidamente ao webhook
        messageProcessor.process(from, userName, text);

        return ResponseEntity.ok().build();
    }

    /**
     * Health check simples.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PoliticoBot online ✅");
    }

    private String extractText(EvolutionWebhookPayload.MessageContent message) {
        if (message == null) return null;
        if (message.getConversation() != null && !message.getConversation().isBlank()) {
            return message.getConversation();
        }
        if (message.getExtendedTextMessage() != null) {
            return message.getExtendedTextMessage().getText();
        }
        return null;
    }
}
