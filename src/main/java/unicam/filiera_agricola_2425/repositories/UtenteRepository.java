package unicam.filiera_agricola_2425.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.filiera_agricola_2425.models.UtenteAutenticato;

import java.util.Optional;

public interface UtenteRepository extends JpaRepository<UtenteAutenticato, Long> {

    // Trova utente per username
    Optional<UtenteAutenticato> findByUsername(String username);
}
