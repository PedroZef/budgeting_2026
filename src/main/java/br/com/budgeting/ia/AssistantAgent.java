package br.com.budgeting.ia;

import br.com.budgeting.ia.tools.TransactionTools;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class AssistantAgent {
    
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;
    private final TransactionTools transactionTools;

    public AssistantAgent(OpenAiAudioTranscriptionModel transcriptionModel, ChatClient.Builder chatClientBuilder, TransactionTools transactionTools) {
        this.transcriptionModel = transcriptionModel;
        this.transactionTools = transactionTools;
        this.chatClient = chatClientBuilder
                .defaultSystem("Você é um assistente financeiro direto e objetivo. Ouça o comando, execute a ferramenta necessária e responda com um texto curto confirmando a ação.")
                .build();
    }

    public String processarComandoDeVoz(Resource arquivoDeAudio) {
        String texto = transcriptionModel.call(new AudioTranscriptionPrompt(arquivoDeAudio)).getResult().getOutput();
        return chatClient.prompt()
                .user(texto)
                .tools(transactionTools)
                .call()
                .content();
    }
}
