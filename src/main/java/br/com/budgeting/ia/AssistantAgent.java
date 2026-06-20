package br.com.budgeting.ia;

import br.com.budgeting.ia.tools.TransactionTools;
import br.com.budgeting.model.Interaction;
import br.com.budgeting.repository.InteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AssistantAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(AssistantAgent.class);
    
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;
    private final TransactionTools transactionTools;
    private final InteractionRepository interactionRepository;

    public AssistantAgent(OpenAiAudioTranscriptionModel transcriptionModel, 
                          ChatClient.Builder chatClientBuilder, 
                          TransactionTools transactionTools,
                          InteractionRepository interactionRepository) {
        this.transcriptionModel = transcriptionModel;
        this.transactionTools = transactionTools;
        this.interactionRepository = interactionRepository;
        this.chatClient = chatClientBuilder
                .defaultSystem("Você é um assistente financeiro inteligente, direto e objetivo. Ouça o comando ou a pergunta, execute a ferramenta necessária (como registrar transação, obter saldo total, obter total gasto por categoria, buscar maior/menor despesa/receita ou obter resumo geral) e responda com um texto curto e natural trazendo as informações solicitadas ou confirmando a ação. " +
                        "Ao registrar a transação, você deve normalizar a categoria para termos padronizados em MAIÚSCULAS. Exemplos de mapeamento:\n" +
                        "- 'comida', 'restaurante', 'almoço', 'jantar', 'lanche' -> 'ALIMENTAÇÃO'\n" +
                        "- 'roupa', 'camiseta', 'vestuário', 'calçado', 'loja' -> 'ROUPAS'\n" +
                        "- 'ações', 'tesouro', 'poupança', 'cripto', 'cdi' -> 'INVESTIMENTOS'\n" +
                        "- 'gasolina', 'uber', 'passagem', 'ônibus', 'pedágio' -> 'TRANSPORTE'\n" +
                        "- 'cinema', 'show', 'jogo', 'festa', 'viagem' -> 'LAZER'\n" +
                        "Mapeie outros termos correlatos de forma inteligente para essas ou novas categorias padronizadas.")
                .build();
    }

    private final java.util.Map<String, LatestInteraction> latestInteractions = new java.util.concurrent.ConcurrentHashMap<>();

    private String getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = (auth != null) ? auth.getName() : null;
        return (name == null || name.equals("anonymousUser")) ? "anonymousUser" : name;
    }

    public String processarComandoDeVoz(Resource arquivoDeAudio) {
        String texto = transcriptionModel.call(new AudioTranscriptionPrompt(arquivoDeAudio)).getResult().getOutput();
        logger.info("[Voice Command] Transcrição do áudio: \"{}\"", texto);
        
        String resposta;
        if (texto == null || texto.trim().isEmpty() || texto.trim().equals(".")) {
            resposta = "Não consegui ouvir o comando de voz. Por favor, fale novamente.";
        } else {
            resposta = executarChat(texto);
        }

        logger.info("[Voice Command] Resposta do Assistente: \"{}\"", resposta);
        
        salvarInteracao("VOICE", (texto != null ? texto : ""), resposta);
        
        this.latestInteractions.put(getUsuarioLogado(), new LatestInteraction("VOICE", (texto != null ? texto : ""), resposta));
        return resposta;
    }

    public String processarComandoDeTexto(String texto) {
        logger.info("[Text Command] Recebido: \"{}\"", texto);
        
        String resposta;
        if (texto == null || texto.trim().isEmpty() || texto.trim().equals(".")) {
            resposta = "Por favor, digite uma pergunta ou comando válido.";
        } else {
            resposta = executarChat(texto);
        }

        logger.info("[Text Command] Resposta do Assistente: \"{}\"", resposta);
        
        salvarInteracao("TEXT", (texto != null ? texto : ""), resposta);
        
        this.latestInteractions.put(getUsuarioLogado(), new LatestInteraction("TEXT", (texto != null ? texto : ""), resposta));
        return resposta;
    }

    private String executarChat(String texto) {
        return chatClient.prompt()
                .user(texto)
                .tools(transactionTools)
                .call()
                .content();
    }

    private void salvarInteracao(String tipo, String pergunta, String resposta) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String usuarioLogado = (auth != null) ? auth.getName() : null;
            String cleanUser = (usuarioLogado == null || usuarioLogado.equals("anonymousUser")) ? null : usuarioLogado;
            
            Interaction interaction = new Interaction(tipo, pergunta, resposta, cleanUser);
            interactionRepository.save(interaction);
        } catch (Exception e) {
            logger.error("Erro ao salvar interação no banco de dados", e);
        }
    }

    public LatestInteraction getLatestInteraction(String username) {
        String key = (username == null || username.equals("anonymousUser")) ? "anonymousUser" : username;
        return this.latestInteractions.get(key);
    }
}

