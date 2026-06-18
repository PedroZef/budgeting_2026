package br.com.budgeting.ia;

import br.com.budgeting.ia.tools.TransactionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class AssistantAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(AssistantAgent.class);
    
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;
    private final TransactionTools transactionTools;

    public AssistantAgent(OpenAiAudioTranscriptionModel transcriptionModel, ChatClient.Builder chatClientBuilder, TransactionTools transactionTools) {
        this.transcriptionModel = transcriptionModel;
        this.transactionTools = transactionTools;
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

    private volatile LatestInteraction latestInteraction;

    public String processarComandoDeVoz(Resource arquivoDeAudio) {
        String texto = transcriptionModel.call(new AudioTranscriptionPrompt(arquivoDeAudio)).getResult().getOutput();
        logger.info("[Voice Command] Transcrição do áudio: \"{}\"", texto);
        
        if (texto == null || texto.trim().isEmpty() || texto.trim().equals(".")) {
            String resposta = "Não consegui ouvir o comando de voz. Por favor, fale novamente.";
            this.latestInteraction = new LatestInteraction("VOICE", (texto != null ? texto : ""), resposta);
            return resposta;
        }

        String resposta = executarChat(texto);
        logger.info("[Voice Command] Resposta do Assistente: \"{}\"", resposta);
        this.latestInteraction = new LatestInteraction("VOICE", texto, resposta);
        return resposta;
    }

    public String processarComandoDeTexto(String texto) {
        logger.info("[Text Command] Recebido: \"{}\"", texto);
        
        if (texto == null || texto.trim().isEmpty() || texto.trim().equals(".")) {
            String resposta = "Por favor, digite uma pergunta ou comando válido.";
            this.latestInteraction = new LatestInteraction("TEXT", (texto != null ? texto : ""), resposta);
            return resposta;
        }

        String resposta = executarChat(texto);
        logger.info("[Text Command] Resposta do Assistente: \"{}\"", resposta);
        this.latestInteraction = new LatestInteraction("TEXT", texto, resposta);
        return resposta;
    }

    private String executarChat(String texto) {
        return chatClient.prompt()
                .user(texto)
                .tools(transactionTools)
                .call()
                .content();
    }

    public LatestInteraction getLatestInteraction() {
        return this.latestInteraction;
    }
}

