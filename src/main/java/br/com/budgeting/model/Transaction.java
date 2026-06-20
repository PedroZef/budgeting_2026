package br.com.budgeting.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String usuario;
    private BigDecimal valor;
    private String categoria;
    private String tipo;
    private LocalDateTime data = LocalDateTime.now();

}