package com.leandrobanin.boteleicoes.service;

import com.leandrobanin.boteleicoes.dto.SendmessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Responsável por enviar mensagens pelo WhatsApp via Evolution API.
 */
@Slf4j
@Service
public class Whatsappservice {

    private final WebClient webClient;
    private final String instanceName;

    public Whatsappservice(
            @Value("${evolution.base-url}") String baseUrl,
            @Value("${evolution.api-key}") String apiKey,
            @Value("${evolution.instance-name}") String instanceName) {

        this.instanceName = instanceName;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Envia uma mensagem de texto para o número informado.
     *
     * @param to   número no formato internacional sem + (ex: "5511999999999")
     * @param text texto da mensagem (suporta formatação WhatsApp: *negrito*, _itálico_)
     */
    public void sendMessage(String to, String text) {
        // Normaliza o número: remove @s.whatsapp.net se vier do webhook
        String number = to.replace("@s.whatsapp.net", "").replace("@g.us", "");

        var request = new SendmessageRequest(number, text, 1200);

        webClient.post()
                .uri("/message/sendText/" + instanceName)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(r -> log.info("Mensagem enviada para {}", number))
                .doOnError(e -> log.error("Erro ao enviar mensagem para {}: {}", number, e.getMessage()))
                .subscribe(); // fire-and-forget assíncrono
    }

    /**
     * Simula digitação antes de enviar (dá sensação mais humana).
     * Chame antes do sendMessage se quiser o efeito.
     */
    public void sendTyping(String to) {
        String number = to.replace("@s.whatsapp.net", "");
        webClient.post()
                .uri("/chat/sendPresence/" + instanceName)
                .bodyValue("""
                    {"number": "%s", "options": {"presence": "composing", "delay": 2000}}
                    """.formatted(number))
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}
