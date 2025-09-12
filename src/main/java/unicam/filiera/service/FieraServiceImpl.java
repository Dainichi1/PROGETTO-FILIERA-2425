package unicam.filiera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unicam.filiera.dto.FieraDto;
import unicam.filiera.entity.FieraEntity;
import unicam.filiera.factory.FieraFactory;
import unicam.filiera.model.Fiera;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.observer.FieraNotifier;
import unicam.filiera.repository.FieraRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FieraServiceImpl implements FieraService {

    private static final Logger log = LoggerFactory.getLogger(FieraServiceImpl.class);

    private final FieraRepository repository;
    private final FieraNotifier notifier;

    @Autowired
    public FieraServiceImpl(FieraRepository repository, FieraNotifier notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }

    @Override
    public void creaFiera(FieraDto dto, String creatore) {
        log.info(">>> Richiesta di creazione nuova fiera da parte di [{}]", creatore);
        log.debug("Dati DTO ricevuti: {}", dto);

        Fiera fiera = FieraFactory.creaFiera(dto, creatore);
        fiera.setStato(StatoEvento.PUBBLICATA);
        log.debug("Oggetto dominio creato: {}", fiera);

        FieraEntity entity = mapToEntity(fiera, dto, null);
        log.info("Salvataggio entity in DB: {}", entity);

        repository.save(entity);

        log.info("Fiera [{}] salvata correttamente con ID={}", entity.getNome(), entity.getId());
        notifier.notificaTutti(fiera, "NUOVA_FIERA_PUBBLICATA");
    }

    @Override
    public void aggiornaFiera(Long id, FieraDto dto, String creatore) {
        log.info(">>> Richiesta di aggiornamento fiera id={} da parte di [{}]", id, creatore);

        FieraEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiera non trovata per la modifica"));

        if (!existing.getCreatoDa().equals(creatore)) {
            log.warn("Tentativo non autorizzato di aggiornamento da [{}]", creatore);
            throw new SecurityException("Non autorizzato a modificare questa fiera");
        }

        log.debug("Entity esistente trovata: {}", existing);

        Fiera updated = FieraFactory.creaFiera(dto, creatore);
        updated.setStato(StatoEvento.PUBBLICATA);

        FieraEntity entity = mapToEntity(updated, dto, existing.getId());
        log.info("Aggiornamento entity in DB: {}", entity);

        repository.save(entity);

        log.info("Fiera [{}] aggiornata correttamente (ID={})", entity.getNome(), entity.getId());
        notifier.notificaTutti(updated, "FIERA_AGGIORNATA");
    }

    @Override
    public List<Fiera> getFiereByCreatore(String creatore) {
        log.debug("Recupero fiere per creatore [{}]", creatore);
        return repository.findByCreatoDa(creatore)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FieraEntity> findEntityById(Long id) {
        log.debug("Ricerca fiera per ID={}", id);
        return repository.findById(id);
    }

    @Override
    public void eliminaById(Long id, String username) {
        log.info(">>> Richiesta di eliminazione fiera id={} da parte di [{}]", id, username);

        FieraEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiera non trovata"));

        if (!username.equals(entity.getCreatoDa())) {
            log.warn("Tentativo non autorizzato di eliminazione da [{}]", username);
            throw new SecurityException("Non autorizzato a eliminare questa fiera");
        }

        Fiera dominio = mapToDomain(entity);
        repository.delete(entity);

        log.info("Fiera eliminata con successo: ID={}, Nome={}", entity.getId(), entity.getNome());
        notifier.notificaTutti(dominio, "FIERA_ELIMINATA");
    }

    // =======================
    // Helpers
    // =======================

    private FieraEntity mapToEntity(Fiera fiera, FieraDto dto, Long id) {
        FieraEntity e = new FieraEntity();
        e.setId(id);
        e.setNome(fiera.getNome());
        e.setDescrizione(fiera.getDescrizione());
        e.setIndirizzo(fiera.getIndirizzo());
        e.setDataInizio(fiera.getDataInizio());
        e.setDataFine(fiera.getDataFine());
        e.setCreatoDa(fiera.getCreatoDa());
        e.setStato(StatoEvento.PUBBLICATA); // sempre PUBBLICATA
        e.setPrezzo(fiera.getPrezzo());
        return e;
    }

    private Fiera mapToDomain(FieraEntity e) {
        return new Fiera.Builder()
                .id(e.getId())
                .nome(e.getNome())
                .descrizione(e.getDescrizione())
                .indirizzo(e.getIndirizzo())
                .dataInizio(e.getDataInizio())
                .dataFine(e.getDataFine())
                .creatoDa(e.getCreatoDa())
                .stato(StatoEvento.PUBBLICATA) // sempre PUBBLICATA
                .prezzo(e.getPrezzo())
                .build();
    }
}
