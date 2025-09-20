package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Ruolo;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<UtenteEntity, String> {

    boolean existsByUsername(String username);

    boolean existsByNomeAndCognome(String nome, String cognome);

    /**
     * Recupera tutti gli utenti filtrati per ruolo singolo.
     */
    List<UtenteEntity> findByRuolo(Ruolo ruolo);

    /**
     * Recupera tutti gli utenti filtrati da una lista di ruoli.
     */
    List<UtenteEntity> findByRuoloIn(List<Ruolo> ruoli);

    Optional<UtenteEntity> findByUsername(String username);

}
