package br.com.budgeting.controller;

import br.com.budgeting.dto.TransactionDTO;
import br.com.budgeting.model.Transaction;
import br.com.budgeting.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @Operation(
        summary = "Listar transações",
        description = "Retorna as transações cadastradas do usuário logado."
    )
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<TransactionDTO>> listar() {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (usuarioLogado == null || usuarioLogado.equals("anonymousUser")) {
            return ResponseEntity.status(401).build();
        }
        List<Transaction> transacoes = service.listarPorUsuario(usuarioLogado);
        List<TransactionDTO> dtos = transacoes.stream()
                .map(TransactionDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(
        summary = "Criar nova transação",
        description = "Cadastra manualmente uma nova transação."
    )
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TransactionDTO> criar(@Valid @RequestBody TransactionDTO dto) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (dto.getUsuario() == null || dto.getUsuario().isEmpty()) {
            dto.setUsuario(usuarioLogado != null && !usuarioLogado.equals("anonymousUser") ? usuarioLogado : "pedro");
        }
        Transaction salva = service.salvar(dto.toEntity());
        return ResponseEntity.ok(new TransactionDTO(salva));
    }

    @Operation(
        summary = "Obter transação por ID",
        description = "Retorna os detalhes de uma transação específica pelo ID."
    )
    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TransactionDTO> obterPorId(@PathVariable Long id) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.buscarPorId(id)
                .map(t -> {
                    if (t.getUsuario() != null && !t.getUsuario().equals(usuarioLogado)) {
                        return ResponseEntity.status(403).<TransactionDTO>build();
                    }
                    return ResponseEntity.ok(new TransactionDTO(t));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Atualizar transação existente",
        description = "Atualiza os dados (valor, categoria, tipo, etc.) de uma transação existente."
    )
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TransactionDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO dto) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.Optional<Transaction> original = service.buscarPorId(id);
        if (original.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (original.get().getUsuario() != null && !original.get().getUsuario().equals(usuarioLogado)) {
            return ResponseEntity.status(403).build();
        }
        try {
            if (usuarioLogado != null && !usuarioLogado.equals("anonymousUser")) {
                dto.setUsuario(usuarioLogado);
            }
            Transaction atualizada = service.atualizar(id, dto.toEntity());
            return ResponseEntity.ok(new TransactionDTO(atualizada));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Deletar transação",
        description = "Remove uma transação pelo ID."
    )
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.Optional<Transaction> original = service.buscarPorId(id);
        if (original.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (original.get().getUsuario() != null && !original.get().getUsuario().equals(usuarioLogado)) {
            return ResponseEntity.status(403).build();
        }
        try {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
