package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;

import java.util.List;

@Repository
public interface UtenteRepository extends JpaRepository<UtenteEntity, String> {

    boolean existsByUsername(String username);

    boolean existsByNomeAndCognome(String nome, String cognome);

    /**
     * Recupera tutti gli utenti filtrati per ruolo.
     */
    List<UtenteEntity> findByRuolo(Ruolo ruolo);
}
