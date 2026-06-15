package br.com.budgeting.controller;

import br.com.budgeting.ia.AssistantAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssistantController.class)
@AutoConfigureMockMvc
class AssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssistantAgent assistantAgent;

    @Test
    @WithMockUser(username = "pedro")
    void deveProcessarComandoDeVozComSucesso() throws Exception {
        // Given
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "test.ogg",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "conteudo_de_audio_fake".getBytes()
        );
        String respostaEsperada = "Sucesso: despesa de R$ 50 salva.";

        when(assistantAgent.processarComandoDeVoz(any(Resource.class))).thenReturn(respostaEsperada);

        // When & Then
        mockMvc.perform(multipart("/api/assistant/voice")
                        .file(audioFile)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(respostaEsperada));
    }
}
