package br.com.budgeting.controller;

import br.com.budgeting.ia.AssistantAgent;
import br.com.budgeting.ia.LatestInteraction;
import br.com.budgeting.model.Interaction;
import br.com.budgeting.repository.InteractionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assistant")
@SecurityRequirement(name = "bearerAuth")
public class AssistantController {

    private static final Logger logger = LoggerFactory.getLogger(AssistantController.class);
    private final AssistantAgent agent;
    private final InteractionRepository interactionRepository;

    public AssistantController(AssistantAgent agent, InteractionRepository interactionRepository) {
        this.agent = agent;
        this.interactionRepository = interactionRepository;
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
            
            logger.info("Operação concluída com sucesso: Comando de voz processado.");
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

    @Operation(
        summary = "Consultas inteligentes e comandos por texto",
        description = "Recebe uma pergunta ou comando financeiro por escrito em texto simples, processa via Inteligência Artificial utilizando ferramentas do sistema para obter saldos, despesas ou registrar transações, e retorna a resposta gerada pela IA."
    )
    @PostMapping(value = "/text", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> handleTextCommand(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Pergunta ou comando financeiro. Exemplos: 'Qual o meu saldo total?', 'Quanto gastei com lazer?', 'Qual foi minha maior despesa?', 'Registrar despesa de 50 reais em alimentação'")
            @RequestBody String commandText) {
        try {
            String resposta = agent.processarComandoDeTexto(commandText);
            logger.info("Operação concluída com sucesso: Comando de texto processado.");
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Obter a última interação do assistente",
        description = "Retorna os detalhes da última interação (de texto ou voz) que foi processada."
    )
    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LatestInteraction> getLatestInteraction() {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        LatestInteraction interaction = agent.getLatestInteraction(usuarioLogado);
        if (interaction == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(interaction);
    }

    @Operation(
        summary = "Listar histórico de interações do assistente",
        description = "Retorna todos os detalhes das interações (de texto ou voz) cadastrados. Se o usuário estiver logado, filtra por ele, caso contrário retorna todas."
    )
    @GetMapping(value = "/interactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Interaction>> listarInteracoes() {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Interaction> interacoes;
        if (usuarioLogado == null || usuarioLogado.equals("anonymousUser")) {
            interacoes = interactionRepository.findAllByOrderByTimestampDesc();
        } else {
            interacoes = interactionRepository.findByUsuarioOrderByTimestampDesc(usuarioLogado);
        }
        return ResponseEntity.ok(interacoes);
    }
}