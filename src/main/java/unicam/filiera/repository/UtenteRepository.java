package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unicam.filiera.entity.UtenteEntity;

public interface UtenteRepository extends JpaRepository<UtenteEntity, String> {
    boolean existsByUsername(String username);
    boolean existsByNomeAndCognome(String nome, String cognome);
}
