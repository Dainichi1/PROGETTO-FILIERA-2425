package unicam.filiera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.entity.VisitaInvitoEntity;
import unicam.filiera.factory.VisitaInvitoFactory;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.model.VisitaInvito;
import unicam.filiera.observer.VisitaInvitoNotifier;
import unicam.filiera.repository.VisitaInvitoRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VisitaInvitoServiceImpl implements VisitaInvitoService {

    private static final Logger log = LoggerFactory.getLogger(VisitaInvitoServiceImpl.class);

    private final VisitaInvitoRepository repository;
    private final VisitaInvitoNotifier notifier;

    @Autowired
    public VisitaInvitoServiceImpl(VisitaInvitoRepository repository, VisitaInvitoNotifier notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }

    @Override
    public void creaVisita(VisitaInvitoDto dto, String creatore) {
        log.info(">>> Richiesta di creazione nuova visita da parte di [{}]", creatore);
        log.debug("Dati DTO ricevuti: {}", dto);

        VisitaInvito visita = VisitaInvitoFactory.creaVisita(dto, creatore);
        visita.setStato(StatoEvento.PUBBLICATA); // sempre PUBBLICATA
        log.debug("Oggetto dominio creato: {}", visita);

        VisitaInvitoEntity entity = mapToEntity(visita, dto, null);
        log.info("Salvataggio entity in DB: {}", entity);

        repository.save(entity);

        log.info("Visita [{}] salvata correttamente con ID={}", entity.getNome(), entity.getId());
        notifier.notificaTutti(visita, "NUOVA_VISITA_PUBBLICATA");
    }

    @Override
    public void aggiornaVisita(Long id, VisitaInvitoDto dto, String creatore) {
        log.info(">>> Richiesta di aggiornamento visita id={} da parte di [{}]", id, creatore);

        VisitaInvitoEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visita non trovata per la modifica"));

        if (!existing.getCreatoDa().equals(creatore)) {
            log.warn("Tentativo non autorizzato di aggiornamento da [{}]", creatore);
            throw new SecurityException("Non autorizzato a modificare questa visita");
        }

        log.debug("Entity esistente trovata: {}", existing);

        VisitaInvito updated = VisitaInvitoFactory.creaVisita(dto, creatore);
        updated.setStato(StatoEvento.PUBBLICATA);

        VisitaInvitoEntity entity = mapToEntity(updated, dto, existing.getId());
        log.info("Aggiornamento entity in DB: {}", entity);

        repository.save(entity);

        log.info("Visita [{}] aggiornata correttamente (ID={})", entity.getNome(), entity.getId());
        notifier.notificaTutti(updated, "VISITA_AGGIORNATA");
    }

    @Override
    public List<VisitaInvito> getVisiteByCreatore(String creatore) {
        log.debug("Recupero visite per creatore [{}]", creatore);
        return repository.findByCreatoDa(creatore)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VisitaInvitoEntity> findEntityById(Long id) {
        log.debug("Ricerca visita per ID={}", id);
        return repository.findById(id);
    }

    @Override
    public void eliminaById(Long id, String username) {
        log.info(">>> Richiesta di eliminazione visita id={} da parte di [{}]", id, username);

        VisitaInvitoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visita non trovata"));

        if (!username.equals(entity.getCreatoDa())) {
            log.warn("Tentativo non autorizzato di eliminazione da [{}]", username);
            throw new SecurityException("Non autorizzato a eliminare questa visita");
        }

        VisitaInvito dominio = mapToDomain(entity);
        repository.delete(entity);

        log.info("Visita eliminata con successo: ID={}, Nome={}", entity.getId(), entity.getNome());
        notifier.notificaTutti(dominio, "VISITA_ELIMINATA");
    }

    // =======================
    // Helpers
    // =======================

    private VisitaInvitoEntity mapToEntity(VisitaInvito visita, VisitaInvitoDto dto, Long id) {
        VisitaInvitoEntity e = new VisitaInvitoEntity();
        e.setId(id);
        e.setNome(visita.getNome());
        e.setDescrizione(visita.getDescrizione());
        e.setIndirizzo(visita.getIndirizzo());
        e.setDataInizio(visita.getDataInizio());
        e.setDataFine(visita.getDataFine());
        e.setCreatoDa(visita.getCreatoDa());
        e.setStato(StatoEvento.PUBBLICATA); // sempre PUBBLICATA
        e.setDestinatari(String.join(",", visita.getDestinatari()));
        return e;
    }

    private VisitaInvito mapToDomain(VisitaInvitoEntity e) {
        return new VisitaInvito.Builder()
                .id(e.getId())
                .nome(e.getNome())
                .descrizione(e.getDescrizione())
                .indirizzo(e.getIndirizzo())
                .dataInizio(e.getDataInizio())
                .dataFine(e.getDataFine())
                .creatoDa(e.getCreatoDa())
                .stato(StatoEvento.PUBBLICATA) // sempre PUBBLICATA
                .destinatari(e.getDestinatari() == null || e.getDestinatari().isBlank()
                        ? List.of()
                        : List.of(e.getDestinatari().split(",")))
                .build();
    }
}
