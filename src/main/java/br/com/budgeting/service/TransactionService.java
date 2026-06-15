package br.com.budgeting.service;

import br.com.budgeting.model.Transaction;
import br.com.budgeting.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction salvar(Transaction transaction) {
        return repository.save(transaction);
    }

}
