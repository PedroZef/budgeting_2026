package br.com.budgeting.repository;

import br.com.budgeting.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUsuarioOrderByDataDesc(String usuario);
}