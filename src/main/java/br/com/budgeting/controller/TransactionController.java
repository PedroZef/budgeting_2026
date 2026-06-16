package br.com.budgeting.controller;

import br.com.budgeting.model.Transaction;
import br.com.budgeting.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
        description = "Retorna todas as transações cadastradas. Se o usuário estiver logado, filtra por ele, caso contrário retorna todas."
    )
    @GetMapping
    public ResponseEntity<List<Transaction>> listar() {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Transaction> transacoes;
        if (usuarioLogado == null || usuarioLogado.equals("anonymousUser")) {
            transacoes = service.buscarTodos();
        } else {
            transacoes = service.listarPorUsuario(usuarioLogado);
        }
        return ResponseEntity.ok(transacoes);
    }

    @Operation(
        summary = "Criar nova transação",
        description = "Cadastra manualmente uma nova transação."
    )
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Transaction> criar(@RequestBody Transaction novaTransacao) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (novaTransacao.getUsuario() == null || novaTransacao.getUsuario().isEmpty()) {
            novaTransacao.setUsuario(usuarioLogado != null && !usuarioLogado.equals("anonymousUser") ? usuarioLogado : "pedro");
        }
        Transaction salva = service.salvar(novaTransacao);
        return ResponseEntity.ok(salva);
    }

    @Operation(
        summary = "Obter transação por ID",
        description = "Retorna os detalhes de uma transação específica pelo ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> obterPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Atualizar transação existente",
        description = "Atualiza os dados (valor, categoria, tipo, etc.) de uma transação existente."
    )
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Transaction> atualizar(
            @PathVariable Long id,
            @RequestBody Transaction dadosAtualizados) {
        try {
            // Se o usuário logado não for anônimo, define-o na transação
            String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
            if (usuarioLogado != null && !usuarioLogado.equals("anonymousUser")) {
                dadosAtualizados.setUsuario(usuarioLogado);
            }
            Transaction atualizada = service.atualizar(id, dadosAtualizados);
            return ResponseEntity.ok(atualizada);
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
        try {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
