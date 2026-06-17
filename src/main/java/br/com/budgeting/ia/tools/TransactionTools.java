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

    @Tool(description = "Retorna o saldo total atual do usuário (Receitas - Despesas). Parâmetro 'justificativa' deve ser uma explicação curta do motivo da consulta.")
    public String obterSaldoTotal(String justificativa) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.List<Transaction> transacoes = service.listarPorUsuario(usuarioLogado);
        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        for (Transaction t : transacoes) {
            if ("RECEITA".equalsIgnoreCase(t.getTipo())) {
                totalReceitas = totalReceitas.add(t.getValor());
            } else if ("DESPESA".equalsIgnoreCase(t.getTipo())) {
                totalDespesas = totalDespesas.add(t.getValor());
            }
        }
        BigDecimal saldo = totalReceitas.subtract(totalDespesas);
        return "Usuário: " + usuarioLogado + ". Total Receitas: R$ " + totalReceitas + ". Total Despesas: R$ " + totalDespesas + ". Saldo Líquido: R$ " + saldo + ".";
    }

    @Tool(description = "Retorna o total gasto em despesas em uma categoria específica. Exemplo de categoria: ALIMENTAÇÃO, TRANSPORTE, LAZER, ROUPAS, INVESTIMENTOS.")
    public String obterTotalGastoPorCategoria(String categoria) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.List<Transaction> transacoes = service.listarPorUsuario(usuarioLogado);
        BigDecimal total = BigDecimal.ZERO;
        String categoriaNorm = categoria.toUpperCase().trim();
        for (Transaction t : transacoes) {
            if ("DESPESA".equalsIgnoreCase(t.getTipo()) && t.getCategoria().toUpperCase().contains(categoriaNorm)) {
                total = total.add(t.getValor());
            }
        }
        return "Usuário: " + usuarioLogado + ". O total gasto na categoria " + categoriaNorm + " é R$ " + total + ".";
    }

    @Tool(description = "Busca a maior despesa (transação com maior valor do tipo DESPESA) do usuário. Parâmetro 'justificativa' deve ser uma explicação curta do motivo da consulta.")
    public String buscarMaiorDespesa(String justificativa) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.List<Transaction> transacoes = service.listarPorUsuario(usuarioLogado);
        Transaction maior = null;
        for (Transaction t : transacoes) {
            if ("DESPESA".equalsIgnoreCase(t.getTipo())) {
                if (maior == null || t.getValor().compareTo(maior.getValor()) > 0) {
                    maior = t;
                }
            }
        }
        if (maior == null) {
            return "Nenhuma despesa encontrada para o usuário " + usuarioLogado + ".";
        }
        return "A maior despesa do usuário " + usuarioLogado + " foi de R$ " + maior.getValor() + " na categoria " + maior.getCategoria() + " em " + maior.getData() + ".";
    }

    @Tool(description = "Retorna um resumo financeiro geral incluindo saldo, total de receitas, total de despesas e as últimas 5 transações. Parâmetro 'justificativa' deve ser uma explicação curta do motivo da consulta.")
    public String obterResumoGeral(String justificativa) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.List<Transaction> transacoes = service.listarPorUsuario(usuarioLogado);
        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        for (Transaction t : transacoes) {
            if ("RECEITA".equalsIgnoreCase(t.getTipo())) {
                totalReceitas = totalReceitas.add(t.getValor());
            } else if ("DESPESA".equalsIgnoreCase(t.getTipo())) {
                totalDespesas = totalDespesas.add(t.getValor());
            }
        }
        BigDecimal saldo = totalReceitas.subtract(totalDespesas);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Resumo Financeiro de ").append(usuarioLogado).append(":\n")
          .append("- Saldo Líquido: R$ ").append(saldo).append("\n")
          .append("- Total Receitas: R$ ").append(totalReceitas).append("\n")
          .append("- Total Despesas: R$ ").append(totalDespesas).append("\n")
          .append("Últimas transações:\n");
          
        int limit = Math.min(transacoes.size(), 5);
        for (int i = 0; i < limit; i++) {
            Transaction t = transacoes.get(i);
            sb.append("* ").append(t.getTipo()).append(": R$ ").append(t.getValor())
              .append(" na categoria ").append(t.getCategoria()).append(" (")
              .append(t.getData()).append(")\n");
        }
        return sb.toString();
    }

    @Tool(description = "Busca a menor despesa (transação com menor valor do tipo DESPESA) do usuário. Parâmetro 'justificativa' deve ser uma explicação curta do motivo da consulta.")
    public String buscarMenorDespesa(String justificativa) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.List<Transaction> transacoes = service.listarPorUsuario(usuarioLogado);
        Transaction menor = null;
        for (Transaction t : transacoes) {
            if ("DESPESA".equalsIgnoreCase(t.getTipo())) {
                if (menor == null || t.getValor().compareTo(menor.getValor()) < 0) {
                    menor = t;
                }
            }
        }
        if (menor == null) {
            return "Nenhuma despesa encontrada para o usuário " + usuarioLogado + ".";
        }
        return "A menor despesa do usuário " + usuarioLogado + " foi de R$ " + menor.getValor() + " na categoria " + menor.getCategoria() + " em " + menor.getData() + ".";
    }

    @Tool(description = "Busca a maior receita (transação com maior valor do tipo RECEITA) do usuário. Parâmetro 'justificativa' deve ser uma explicação curta do motivo da consulta.")
    public String buscarMaiorReceita(String justificativa) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.List<Transaction> transacoes = service.listarPorUsuario(usuarioLogado);
        Transaction maior = null;
        for (Transaction t : transacoes) {
            if ("RECEITA".equalsIgnoreCase(t.getTipo())) {
                if (maior == null || t.getValor().compareTo(maior.getValor()) > 0) {
                    maior = t;
                }
            }
        }
        if (maior == null) {
            return "Nenhuma receita encontrada para o usuário " + usuarioLogado + ".";
        }
        return "A maior receita do usuário " + usuarioLogado + " foi de R$ " + maior.getValor() + " na categoria " + maior.getCategoria() + " em " + maior.getData() + ".";
    }

    @Tool(description = "Busca a menor receita (transação com menor valor do tipo RECEITA) do usuário. Parâmetro 'justificativa' deve ser uma explicação curta do motivo da consulta.")
    public String buscarMenorReceita(String justificativa) {
        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.List<Transaction> transacoes = service.listarPorUsuario(usuarioLogado);
        Transaction menor = null;
        for (Transaction t : transacoes) {
            if ("RECEITA".equalsIgnoreCase(t.getTipo())) {
                if (menor == null || t.getValor().compareTo(menor.getValor()) < 0) {
                    menor = t;
                }
            }
        }
        if (menor == null) {
            return "Nenhuma receita encontrada para o usuário " + usuarioLogado + ".";
        }
        return "A menor receita do usuário " + usuarioLogado + " foi de R$ " + menor.getValor() + " na categoria " + menor.getCategoria() + " em " + menor.getData() + ".";
    }
}
