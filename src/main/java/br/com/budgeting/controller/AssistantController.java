package br.com.budgeting.controller;

import br.com.budgeting.ia.AssistantAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantAgent agent;

    public AssistantController(AssistantAgent agent) {
        this.agent = agent;
    }

    @Operation(
        summary = "Processar comando de voz",
        description = "Recebe um arquivo de áudio contendo um comando financeiro, transcreve para texto, usa a IA para interpretar e salva a transação no banco de dados."
    )
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<String> handleVoiceCommand(
            @Parameter(description = "Arquivo de áudio (.ogg, .wav, .mp3) com o comando de voz")
            @RequestPart("audio") MultipartFile audioFile) {
        try {
            // Cria um arquivo temporário com a mesma extensão do arquivo original
            String originalFilename = audioFile.getOriginalFilename();
            String extension = ".ogg"; // fallback default
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            java.io.File tempFile = java.io.File.createTempFile("voice_", extension);
            audioFile.transferTo(tempFile);

            String resposta = agent.processarComandoDeVoz(new org.springframework.core.io.FileSystemResource(tempFile));
            
            tempFile.delete(); // Limpa o arquivo temp depois de usar
            
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            e.printStackTrace();
            String detalhe = e.getMessage();
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause != rootCause.getCause()) {
                rootCause = rootCause.getCause();
            }
            detalhe += " | Erro Real: " + rootCause.getMessage();
            return ResponseEntity.internalServerError().body("Erro: " + detalhe);
        }
    }
}