package unicam.filiera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unicam.filiera.dto.PrenotazioneVisitaDto;
import unicam.filiera.entity.PrenotazioneVisitaEntity;
import unicam.filiera.repository.PrenotazioneVisitaRepository;
import unicam.filiera.validation.PrenotazioneVisitaValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrenotazioneVisitaServiceImpl implements PrenotazioneVisitaService {

    private static final Logger log = LoggerFactory.getLogger(PrenotazioneVisitaServiceImpl.class);

    private final PrenotazioneVisitaRepository repository;

    @Autowired
    public PrenotazioneVisitaServiceImpl(PrenotazioneVisitaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void creaPrenotazione(PrenotazioneVisitaDto dto, String usernameVenditore) {
        log.info(">>> Richiesta di creazione prenotazione visita da [{}] per visita id={}",
                usernameVenditore, dto.getIdVisita());

        // Verifica se esiste già una prenotazione per questa visita e venditore
        boolean alreadyExists = repository.existsByIdVisitaAndUsernameVenditore(dto.getIdVisita(), usernameVenditore);

        // Validazione con controllo duplicati
        PrenotazioneVisitaValidator.valida(dto, alreadyExists);

        // Mapping DTO -> Entity
        PrenotazioneVisitaEntity entity = new PrenotazioneVisitaEntity();
        entity.setIdVisita(dto.getIdVisita());
        entity.setNumeroPersone(dto.getNumeroPersone());
        entity.setUsernameVenditore(usernameVenditore);
        entity.setDataPrenotazione(LocalDateTime.now());

        repository.save(entity);

        log.info("Prenotazione visita salvata correttamente con ID={}", entity.getId());
    }

    @Override
    public List<PrenotazioneVisitaDto> getPrenotazioniByVenditore(String usernameVenditore) {
        log.debug("Recupero prenotazioni per venditore [{}]", usernameVenditore);
        return repository.findByUsernameVenditore(usernameVenditore)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PrenotazioneVisitaDto> getPrenotazioniByVisita(Long idVisita) {
        log.debug("Recupero prenotazioni per visita id={}", idVisita);
        return repository.findByIdVisita(idVisita)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PrenotazioneVisitaEntity> findById(Long id) {
        log.debug("Ricerca prenotazione per ID={}", id);
        return repository.findById(id);
    }

    @Override
    public void eliminaById(Long idPrenotazione, String usernameVenditore) {
        log.info(">>> Richiesta di eliminazione prenotazione id={} da parte di [{}]",
                idPrenotazione, usernameVenditore);

        PrenotazioneVisitaEntity entity = repository.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

        if (!usernameVenditore.equals(entity.getUsernameVenditore())) {
            log.warn("Tentativo non autorizzato di eliminazione da [{}]", usernameVenditore);
            throw new SecurityException("Non autorizzato a eliminare questa prenotazione");
        }

        repository.delete(entity);

        log.info("Prenotazione eliminata con successo: ID={}, Visita={}",
                entity.getId(), entity.getIdVisita());
    }

    // =======================
    // Helpers
    // =======================

    private PrenotazioneVisitaDto mapToDto(PrenotazioneVisitaEntity e) {
        return new PrenotazioneVisitaDto(
                e.getId(),
                e.getIdVisita(),
                e.getNumeroPersone(),
                e.getUsernameVenditore(),
                e.getDataPrenotazione()
        );
    }
}
