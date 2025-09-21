package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.PrenotazioneFieraEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrenotazioneFieraRepository extends JpaRepository<PrenotazioneFieraEntity, Long> {

    // tutte le prenotazioni fatte da un acquirente
    List<PrenotazioneFieraEntity> findByUsernameAcquirente(String usernameAcquirente);

    // tutte le prenotazioni per una fiera
    List<PrenotazioneFieraEntity> findByIdFiera(Long idFiera);

    // verifica se un acquirente ha già prenotato una fiera
    Optional<PrenotazioneFieraEntity> findByIdFieraAndUsernameAcquirente(Long idFiera, String usernameAcquirente);

    // controlla se esiste già una prenotazione (per validatore)
    boolean existsByIdFieraAndUsernameAcquirente(Long idFiera, String usernameAcquirente);
}
