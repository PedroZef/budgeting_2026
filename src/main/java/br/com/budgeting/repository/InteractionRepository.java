package br.com.budgeting.repository;

import br.com.budgeting.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByUsuarioOrderByTimestampDesc(String usuario);
    List<Interaction> findAllByOrderByTimestampDesc();
}
