package br.com.budgeting.ia.tools;

import br.com.budgeting.model.Transaction;
import br.com.budgeting.service.TransactionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionTools {

    private final TransactionService service;

    public TransactionTools(TransactionService service) {
        this.service = service;
    }

    @Tool(description = "Registra uma nova transação financeira. O tipo deve ser RECEITA ou DESPESA.")
    public String registrarTransacao(BigDecimal valor, String categoria, String tipo) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Transaction t = new Transaction();
        t.setUsuario(usuarioLogado);
        t.setValor(valor);
        t.setCategoria(categoria);
        t.setTipo(tipo);
        
        service.salvar(t);
        return "Sucesso: " + tipo + " de R$ " + valor + " na categoria " + categoria + " salva para o usuário " + usuarioLogado + ".";
    }
}
