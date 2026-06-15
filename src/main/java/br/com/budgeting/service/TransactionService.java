package br.com.budgeting.service;

import br.com.budgeting.model.Transaction;
import br.com.budgeting.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction salvar(Transaction transaction) {
        return repository.save(transaction);
    }

    public List<Transaction> buscarTodos() {
        return repository.findAll();
    }

    public List<Transaction> listarPorUsuario(String usuario) {
        return repository.findByUsuarioOrderByDataDesc(usuario);
    }

    public Optional<Transaction> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Transaction atualizar(Long id, Transaction dadosAtualizados) {
        return repository.findById(id)
                .map(t -> {
                    t.setValor(dadosAtualizados.getValor());
                    t.setCategoria(dadosAtualizados.getCategoria());
                    t.setTipo(dadosAtualizados.getTipo());
                    if (dadosAtualizados.getUsuario() != null) {
                        t.setUsuario(dadosAtualizados.getUsuario());
                    }
                    if (dadosAtualizados.getData() != null) {
                        t.setData(dadosAtualizados.getData());
                    }
                    return repository.save(t);
                })
                .orElseThrow(() -> new RuntimeException("Transação não encontrada com id: " + id));
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}
