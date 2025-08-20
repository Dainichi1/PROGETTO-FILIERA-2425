package unicam.progetto_filiera_springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.progetto_filiera_springboot.domain.model.Utente;
import unicam.progetto_filiera_springboot.domain.model.Ruolo;

import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, String> {

    // per il check duplicato in register()
    boolean existsByUsernameAndRuolo(String username, Ruolo ruolo);

    // per il login con ruolo
    Optional<Utente> findByUsernameAndRuolo(String username, Ruolo ruolo);

    // utile altrove
    Optional<Utente> findByUsername(String username);
}
