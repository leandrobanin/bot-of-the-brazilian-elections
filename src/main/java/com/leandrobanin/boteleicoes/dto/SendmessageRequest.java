package com.leandrobanin.boteleicoes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Payload para enviar mensagem
// POST /message/sendText/{instance}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendmessageRequest {

    @JsonProperty("number")
    private String number;

    @JsonProperty("text")
    private String text;

    // delay na msg(ms)
    @JsonProperty("delay")
    private Integer delay = 1200;
}
