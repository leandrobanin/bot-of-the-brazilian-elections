package com.leandrobanin.boteleicoes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


// Payload que envia no webhook
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvolutionWebhookPayload {

    private String event;
    private String instance;
    private MessageData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageData {

        @JsonProperty("key")
        private MessageKey key;

        @JsonProperty("message")
        private MessageContent message;

        @JsonProperty("pushName")
        private String pushName;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageKey {
        private String remoteJid;
        private boolean fromMe;
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageContent {
        private String conversation;
        private ExtendedText extendedTextMessage;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtendedText {
        private String text;
    }
}