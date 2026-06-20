package br.com.budgeting.dto;

import br.com.budgeting.model.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    
    private String usuario;

    @NotNull(message = "O valor da transação é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior ou igual a 0.01.")
    private BigDecimal valor;

    @NotBlank(message = "A categoria é obrigatória.")
    private String categoria;

    @NotBlank(message = "O tipo é obrigatório.")
    @Pattern(regexp = "^(?i)(RECEITA|DESPESA)$", message = "O tipo deve ser RECEITA ou DESPESA.")
    private String tipo;

    private LocalDateTime data;

    public TransactionDTO(Transaction transaction) {
        if (transaction != null) {
            this.id = transaction.getId();
            this.usuario = transaction.getUsuario();
            this.valor = transaction.getValor();
            this.categoria = transaction.getCategoria();
            this.tipo = transaction.getTipo() != null ? transaction.getTipo().toUpperCase() : null;
            this.data = transaction.getData();
        }
    }

    public Transaction toEntity() {
        Transaction transaction = new Transaction();
        transaction.setId(this.id);
        transaction.setUsuario(this.usuario);
        transaction.setValor(this.valor);
        transaction.setCategoria(this.categoria);
        transaction.setTipo(this.tipo != null ? this.tipo.toUpperCase() : null);
        if (this.data != null) {
            transaction.setData(this.data);
        }
        return transaction;
    }
}
