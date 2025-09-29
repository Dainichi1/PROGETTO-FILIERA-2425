package unicam.filiera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicam.filiera.dto.PrenotazioneFieraDto;
import unicam.filiera.entity.FieraEntity;
import unicam.filiera.entity.PrenotazioneFieraEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.repository.FieraRepository;
import unicam.filiera.repository.PrenotazioneFieraRepository;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.validation.PrenotazioneFieraValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrenotazioneFieraServiceImpl implements PrenotazioneFieraService {

    private static final Logger log = LoggerFactory.getLogger(PrenotazioneFieraServiceImpl.class);

    private final PrenotazioneFieraRepository prenotazioneRepository;
    private final FieraRepository fieraRepository;
    private final UtenteRepository utenteRepository;

    @Autowired
    public PrenotazioneFieraServiceImpl(
            PrenotazioneFieraRepository prenotazioneRepository,
            FieraRepository fieraRepository,
            UtenteRepository utenteRepository
    ) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.fieraRepository = fieraRepository;
        this.utenteRepository = utenteRepository;
    }

    @Override
    @Transactional
    public double creaPrenotazione(PrenotazioneFieraDto dto, String usernameAcquirente) {
        log.info(">>> Richiesta di prenotazione fiera da [{}] per fiera id={} ({} persone)",
                usernameAcquirente, dto.getIdFiera(), dto.getNumeroPersone());

        // Validazione
        PrenotazioneFieraValidator.valida(dto);

        // Recupero fiera
        FieraEntity fiera = fieraRepository.findById(dto.getIdFiera())
                .orElseThrow(() -> new IllegalArgumentException("Fiera non trovata"));

        double costoTotale = fiera.getPrezzo() * dto.getNumeroPersone();

        // Recupero utente
        UtenteEntity utente = utenteRepository.findByUsername(usernameAcquirente)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        // Verifica fondi
        if (utente.getFondi() < costoTotale) {
            throw new IllegalArgumentException("⚠ Fondi insufficienti per prenotare la fiera.");
        }

        // Scala fondi
        utente.setFondi(utente.getFondi() - costoTotale);
        utenteRepository.save(utente);

        // Salva prenotazione
        PrenotazioneFieraEntity entity = new PrenotazioneFieraEntity();
        entity.setIdFiera(dto.getIdFiera());
        entity.setNumeroPersone(dto.getNumeroPersone());
        entity.setUsernameAcquirente(usernameAcquirente);
        entity.setDataPrenotazione(LocalDateTime.now());

        prenotazioneRepository.save(entity);

        log.info("Prenotazione fiera salvata con ID={}, costoTotale={}, fondiRimanenti={}",
                entity.getId(), costoTotale, utente.getFondi());

        return utente.getFondi();
    }

    @Override
    public List<PrenotazioneFieraDto> getPrenotazioniByAcquirente(String usernameAcquirente) {
        log.debug("Recupero prenotazioni fiere per acquirente [{}]", usernameAcquirente);
        return prenotazioneRepository.findByUsernameAcquirente(usernameAcquirente)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PrenotazioneFieraDto> getPrenotazioniByFiera(Long idFiera) {
        log.debug("Recupero prenotazioni per fiera id={}", idFiera);
        return prenotazioneRepository.findByIdFiera(idFiera)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PrenotazioneFieraEntity> findById(Long id) {
        log.debug("Ricerca prenotazione fiera per ID={}", id);
        return prenotazioneRepository.findById(id);
    }

    @Override
    @Transactional
    public double eliminaById(Long idPrenotazione, String usernameAcquirente) {
        log.info(">>> Richiesta di eliminazione prenotazione fiera id={} da parte di [{}]",
                idPrenotazione, usernameAcquirente);

        PrenotazioneFieraEntity entity = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

        if (!usernameAcquirente.equals(entity.getUsernameAcquirente())) {
            log.warn("Tentativo non autorizzato di eliminazione da [{}]", usernameAcquirente);
            throw new SecurityException("Non autorizzato a eliminare questa prenotazione");
        }

        UtenteEntity utente = utenteRepository.findByUsername(usernameAcquirente)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        // Calcolo rimborso
        double rimborso = fieraRepository.findById(entity.getIdFiera())
                .map(fiera -> fiera.getPrezzo() * entity.getNumeroPersone())
                .orElse(0.0);

        // Aggiorna fondi
        utente.setFondi(utente.getFondi() + rimborso);
        utenteRepository.save(utente);

        // Elimina prenotazione
        prenotazioneRepository.delete(entity);

        log.info("Prenotazione fiera eliminata con successo: ID={}, Fiera={}, Rimborso={}, FondiRimanenti={}",
                entity.getId(), entity.getIdFiera(), rimborso, utente.getFondi());

        return utente.getFondi();
    }

    // =======================
    // Helpers
    // =======================
    private PrenotazioneFieraDto mapToDto(PrenotazioneFieraEntity e) {
        PrenotazioneFieraDto dto = new PrenotazioneFieraDto(
                e.getId(),
                e.getIdFiera(),
                e.getNumeroPersone(),
                e.getUsernameAcquirente(),
                e.getDataPrenotazione()
        );

        fieraRepository.findById(e.getIdFiera()).ifPresent(fiera -> {
            dto.setNomeFiera(fiera.getNome());
            dto.setPrezzoFiera(fiera.getPrezzo());
        });

        return dto;
    }
}
