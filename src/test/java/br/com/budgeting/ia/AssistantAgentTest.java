package br.com.budgeting.ia;

import br.com.budgeting.ia.tools.TransactionTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;

import br.com.budgeting.repository.InteractionRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssistantAgentTest {

    @Mock
    private OpenAiAudioTranscriptionModel transcriptionModel;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private TransactionTools transactionTools;

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private Resource audioResource;

    private AssistantAgent assistantAgent;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        assistantAgent = new AssistantAgent(transcriptionModel, chatClientBuilder, transactionTools, interactionRepository);
    }

    @Test
    void deveProcessarComandoDeVozComSucesso() {
        // Given
        String textoTranscrevido = "Registrar despesa de R$ 50 em alimentação";
        String respostaEsperada = "Despesa registrada com sucesso!";

        // Mocking transcription
        AudioTranscriptionResponse transcriptionResponse = mock(AudioTranscriptionResponse.class);
        AudioTranscription transcriptionResult = mock(AudioTranscription.class);
        
        when(transcriptionModel.call(any(AudioTranscriptionPrompt.class))).thenReturn(transcriptionResponse);
        when(transcriptionResponse.getResult()).thenReturn(transcriptionResult);
        when(transcriptionResult.getOutput()).thenReturn(textoTranscrevido);

        // Mocking chatClient fluent API com RETURNS_DEEP_STUBS
        when(chatClient.prompt()
                .user(textoTranscrevido)
                .tools(transactionTools)
                .call()
                .content()
        ).thenReturn(respostaEsperada);

        // When
        String resposta = assistantAgent.processarComandoDeVoz(audioResource);

        // Then
        assertEquals(respostaEsperada, resposta);
        verify(transcriptionModel, times(1)).call(any(AudioTranscriptionPrompt.class));
    }
}
