package unicam.progetto_filiera_springboot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.progetto_filiera_springboot.domain.model.Utente;
import unicam.progetto_filiera_springboot.domain.model.Ruolo;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    boolean existsByUsernameAndRuolo(String username, Ruolo ruolo);

    Optional<Utente> findByUsernameAndRuolo(String username, Ruolo ruolo);
}
