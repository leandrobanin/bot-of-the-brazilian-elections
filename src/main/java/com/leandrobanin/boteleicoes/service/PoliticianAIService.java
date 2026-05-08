package com.leandrobanin.boteleicoes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Responsável por gerar as respostas sobre os políticos usando IA.
 * Usa @Cacheable para não chamar a API toda vez que o mesmo nome for consultado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PoliticianAIService {

    private final AnthropicChatModel chatModel;

    // ─────────────────────────────────────────────────────────────
    // SYSTEM PROMPTS
    // ─────────────────────────────────────────────────────────────

    private static final String SYSTEM_RESUMO = """
        Você é um assistente político imparcial e objetivo para um bot de WhatsApp brasileiro.
        Seu papel é informar eleitores sobre candidatos às eleições presidenciais de 2026.
        
        REGRAS OBRIGATÓRIAS:
        - Seja NEUTRO. Não elogie nem critique o candidato.
        - Limite a resposta a no máximo 5 pontos curtos.
        - Use emojis simples para facilitar a leitura no WhatsApp.
        - Nunca invente informações. Se não souber algo com certeza, diga "informação não confirmada".
        - Termine SEMPRE com: "📩 Responda *mais* para saber detalhes"
        - Escreva em português brasileiro informal, como se fosse uma mensagem de WhatsApp.
        - NÃO use markdown como **negrito** ou # cabeçalhos. Use *asterisco* para negrito no WhatsApp.
        
        FORMATO DA RESPOSTA:
        🗳️ *[NOME DO POLÍTICO]*
        Partido: [sigla] | [cargo atual]
        
        📌 [ponto 1 - máx 1 linha]
        📌 [ponto 2 - máx 1 linha]
        📌 [ponto 3 - máx 1 linha]
        📌 [ponto 4 - máx 1 linha]
        📌 [ponto 5 - máx 1 linha]
        
        📩 Responda *mais* para saber detalhes
        """;

    private static final String SYSTEM_DETALHES = """
        Você é um assistente político imparcial para um bot de WhatsApp brasileiro.
        O usuário pediu mais detalhes sobre um candidato que já recebeu o resumo.
        
        REGRAS OBRIGATÓRIAS:
        - Seja NEUTRO e factual.
        - Organize em 3 blocos: Carreira, Propostas e Polêmicas/Críticas.
        - Use linguagem simples e direta, como WhatsApp.
        - Máximo 3 pontos por bloco.
        - Use *asterisco* para negrito (formato WhatsApp).
        - NÃO use markdown com # ou **.
        - Termine com: "🔎 Quer comparar com outro candidato? Mande o nome!"
        
        FORMATO:
        📋 *Detalhes: [NOME]*
        
        👤 *Carreira*
        • [ponto]
        • [ponto]
        
        📢 *Propostas principais*
        • [ponto]
        • [ponto]
        
        ⚠️ *Críticas / Polêmicas*
        • [ponto]
        • [ponto]
        
        🔎 Quer comparar com outro candidato? Mande o nome!
        """;

    private static final String SYSTEM_NAO_ENCONTRADO = """
        Você é um assistente de WhatsApp. O usuário mandou uma mensagem mas não ficou claro
        se é o nome de um político candidato à presidência do Brasil em 2026.
        Responda educadamente dizendo que não reconheceu o nome e peça para confirmar.
        Seja breve (máximo 2 linhas). Use linguagem informal brasileira.
        Sugira alguns exemplos de candidatos conhecidos.
        """;

    // ─────────────────────────────────────────────────────────────
    // MÉTODOS PÚBLICOS
    // ─────────────────────────────────────────────────────────────

    /**
     * Gera o card resumo do político.
     * Resultado cacheado por nome (case-insensitive normalizado).
     */
    @Cacheable(value = "resumo-politico", key = "#nomePolitico.toLowerCase().trim()")
    public String gerarResumo(String nomePolitico) {
        log.info("Gerando resumo para: {}", nomePolitico);

        var prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM_RESUMO),
                new UserMessage("Me dê o card resumo de: " + nomePolitico)
        ));

        return Objects.requireNonNull(chatModel.call(prompt).getResult()).getOutput().getText();
    }

    /**
     * Gera detalhes aprofundados do político.
     * Também cacheado.
     */
    @Cacheable(value = "detalhes-politico", key = "#nomePolitico.toLowerCase().trim()")
    public String gerarDetalhes(String nomePolitico) {
        log.info("Gerando detalhes para: {}", nomePolitico);

        var prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM_DETALHES),
                new UserMessage("Detalhes completos sobre: " + nomePolitico)
        ));

        return Objects.requireNonNull(chatModel.call(prompt).getResult()).getOutput().getText();
    }

    /**
     * Gera uma resposta de "não encontrado / não entendi".
     * Não é cacheado pois a mensagem varia.
     */
    public String gerarNaoEncontrado(String mensagemOriginal) {
        var prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM_NAO_ENCONTRADO),
                new UserMessage("Mensagem do usuário: " + mensagemOriginal)
        ));

        return Objects.requireNonNull(chatModel.call(prompt).getResult()).getOutput().getText();
    }
}
