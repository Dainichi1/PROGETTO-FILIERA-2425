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
        log.info(">>> Creazione nuova visita da parte di [{}]", creatore);

        var visita = VisitaInvitoFactory.creaVisita(dto, creatore);
        visita.setStato(StatoEvento.PUBBLICATA); // default

        VisitaInvitoEntity entity = mapToEntity(visita, dto, null);
        repository.save(entity);

        notifier.notificaTutti(visita, "NUOVA_VISITA_PUBBLICATA");
    }

    @Override
    public void aggiornaVisita(Long id, VisitaInvitoDto dto, String creatore) {
        var existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visita non trovata per la modifica"));

        if (!existing.getCreatoDa().equals(creatore)) {
            throw new SecurityException("Non autorizzato a modificare questa visita");
        }

        var updated = VisitaInvitoFactory.creaVisita(dto, creatore);
        updated.setStato(StatoEvento.PUBBLICATA);

        VisitaInvitoEntity entity = mapToEntity(updated, dto, existing.getId());
        repository.save(entity);

        notifier.notificaTutti(updated, "VISITA_AGGIORNATA");
    }

    @Override
    public List<VisitaInvitoDto> getVisiteByCreatore(String creatore) {
        return repository.findByCreatoDa(creatore)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<VisitaInvitoDto> getVisiteByStato(StatoEvento stato) {
        return repository.findByStato(stato)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<VisitaInvitoDto> getVisiteByRuoloDestinatario(String ruolo) {
        return repository.findByRuoloAndStato(ruolo, StatoEvento.PUBBLICATA)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public Optional<VisitaInvitoEntity> findEntityById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<VisitaInvitoDto> findDtoById(Long id) {
        return repository.findById(id).map(this::mapToDto);
    }

    @Override
    public void eliminaById(Long id, String username) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visita non trovata"));

        if (!username.equals(entity.getCreatoDa())) {
            throw new SecurityException("Non autorizzato a eliminare questa visita");
        }

        repository.delete(entity);
        notifier.notificaTutti(mapToDomain(entity), "VISITA_ELIMINATA");
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
        e.setStato(visita.getStato() != null ? visita.getStato() : StatoEvento.PUBBLICATA);
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
                .stato(e.getStato())
                .destinatari(e.getDestinatari() == null || e.getDestinatari().isBlank()
                        ? List.of()
                        : List.of(e.getDestinatari().split(",")))
                .build();
    }

    private VisitaInvitoDto mapToDto(VisitaInvitoEntity e) {
        VisitaInvitoDto dto = new VisitaInvitoDto();
        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setDescrizione(e.getDescrizione());
        dto.setIndirizzo(e.getIndirizzo());
        dto.setDataInizio(e.getDataInizio());
        dto.setDataFine(e.getDataFine());
        dto.setCreatoDa(e.getCreatoDa());
        dto.setStato(e.getStato());
        dto.setDestinatari(e.getDestinatari() == null || e.getDestinatari().isBlank()
                ? List.of()
                : List.of(e.getDestinatari().split(",")));
        return dto;
    }
}
