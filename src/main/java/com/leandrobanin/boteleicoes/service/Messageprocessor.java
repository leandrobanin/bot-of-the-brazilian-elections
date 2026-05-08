package com.leandrobanin.boteleicoes.service;

import com.leandrobanin.boteleicoes.model.Usersession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Cérebro do bot: recebe mensagem do usuário e decide o que fazer.
 *
 * Fluxo:
 *   1. Usuário manda nome de político  → resumo + set state AWAITING_MORE
 *   2. Usuário manda "mais"            → detalhes do último político
 *   3. Qualquer outra coisa            → tenta identificar ou pede para repetir
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Messageprocessor {

    private final PoliticianAIService aiService;
    private final Whatsappservice whatsAppService;
    private final Sessionservice sessionService;

    // Palavras que o usuário pode mandar para pedir mais detalhes
    private static final String[] TRIGGER_MAIS = {
            "mais", "more", "detalhes", "detalhar", "quero mais",
            "me conta mais", "continua", "saiba mais", "quero saber mais"
    };

    // Mensagem de boas-vindas / ajuda
    private static final String MSG_AJUDA = """
        👋 Olá! Sou o *PoliticoBot* 🗳️
        
        Me mande o nome de qualquer candidato à presidência do Brasil em 2026 e te mostro as informações principais!
        
        Exemplos:
        • Lula
        • Bolsonaro
        • Pablo Marçal
        • Tarcísio de Freitas
        
        Pode mandar o nome! 👇
        """;

    /**
     * Ponto de entrada: processa uma mensagem recebida.
     */
    public void process(String from, String userName, String messageText) {
        if (messageText == null || messageText.isBlank()) return;

        String text = messageText.trim();
        String textLower = text.toLowerCase();
        Usersession session = sessionService.getOrCreate(from);

        log.info("[{}] Mensagem: '{}' | Estado: {}", from, text, session.getState());

        // ── Comandos de ajuda ──────────────────────────────────────────
        if (textLower.matches("(oi|olá|ola|boa|bom dia|boa tarde|boa noite|help|ajuda|inicio|início|menu)")) {
            whatsAppService.sendMessage(from, MSG_AJUDA);
            sessionService.resetSession(from);
            return;
        }

        // ── Usuário pediu "mais" sobre o último político ───────────────
        if (session.getState() == Usersession.State.AWAITING_MORE && isPedidoMais(textLower)) {
            String politico = session.getLastPolitician();
            if (politico != null) {
                whatsAppService.sendTyping(from);
                String detalhes = aiService.gerarDetalhes(politico);
                whatsAppService.sendMessage(from, detalhes);

                // Volta para IDLE depois de dar os detalhes
                session.setState(Usersession.State.IDLE);
                sessionService.update(session);
            }
            return;
        }

        // ── Detecta nome de político e gera resumo ─────────────────────
        // Heurística: se a mensagem tem entre 2 e 6 palavras, provavelmente é um nome
        String[] palavras = text.split("\\s+");
        if (palavras.length >= 1 && palavras.length <= 8) {
            try {
                whatsAppService.sendTyping(from);
                String resumo = aiService.gerarResumo(text);

                // Verifica se a IA disse que não conhece (quando ela retorna msg de erro)
                if (resumo.toLowerCase().contains("não encontrei") ||
                        resumo.toLowerCase().contains("não reconheci") ||
                        resumo.toLowerCase().contains("não é candidato")) {
                    whatsAppService.sendMessage(from, resumo);
                    return;
                }

                whatsAppService.sendMessage(from, resumo);

                // Salva estado para possível "quero mais"
                session.setState(Usersession.State.AWAITING_MORE);
                session.setLastPolitician(text);
                sessionService.update(session);

            } catch (Exception e) {
                log.error("Erro ao gerar resumo: {}", e.getMessage());
                whatsAppService.sendMessage(from, "⚠️ Ops! Tive um problema ao buscar as infos. Tenta de novo em instantes.");
            }
            return;
        }

        // ── Fallback: não entendeu ─────────────────────────────────────
        String naoEntendeu = aiService.gerarNaoEncontrado(text);
        whatsAppService.sendMessage(from, naoEntendeu);
    }

    private boolean isPedidoMais(String textLower) {
        for (String trigger : TRIGGER_MAIS) {
            if (textLower.contains(trigger)) {
                return true;
            }
        }
        return false;
    }
}
