package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.PrenotazioneVisitaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrenotazioneVisitaRepository extends JpaRepository<PrenotazioneVisitaEntity, Long> {

    // tutte le prenotazioni fatte da un venditore
    List<PrenotazioneVisitaEntity> findByUsernameVenditore(String usernameVenditore);

    // tutte le prenotazioni per una visita
    List<PrenotazioneVisitaEntity> findByIdVisita(Long idVisita);

    // verifica se un venditore ha già prenotato una visita
    Optional<PrenotazioneVisitaEntity> findByIdVisitaAndUsernameVenditore(Long idVisita, String usernameVenditore);

    // controlla se esiste già una prenotazione (per validatore)
    boolean existsByIdVisitaAndUsernameVenditore(Long idVisita, String usernameVenditore);
}
