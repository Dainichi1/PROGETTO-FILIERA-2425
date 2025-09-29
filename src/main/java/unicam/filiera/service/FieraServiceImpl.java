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
        log.info(">>> Creazione nuova fiera da parte di [{}]", creatore);

        Fiera fiera = FieraFactory.creaFiera(dto, creatore);
        fiera.setStato(StatoEvento.PUBBLICATA); // default

        FieraEntity entity = mapToEntity(fiera, dto, null);
        repository.save(entity);

        notifier.notificaTutti(fiera, "NUOVA_FIERA_PUBBLICATA");
    }

    @Override
    public void aggiornaFiera(Long id, FieraDto dto, String creatore) {
        var existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiera non trovata per la modifica"));

        if (!existing.getCreatoDa().equals(creatore)) {
            throw new SecurityException("Non autorizzato a modificare questa fiera");
        }

        Fiera updated = FieraFactory.creaFiera(dto, creatore);
        updated.setStato(StatoEvento.PUBBLICATA);

        FieraEntity entity = mapToEntity(updated, dto, existing.getId());
        repository.save(entity);

        notifier.notificaTutti(updated, "FIERA_AGGIORNATA");
    }

    @Override
    public List<FieraDto> getFiereByCreatore(String creatore) {
        return repository.findByCreatoDa(creatore)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public Optional<FieraEntity> findEntityById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<FieraDto> findDtoById(Long id) {
        return repository.findById(id).map(this::mapToDto);
    }

    @Override
    public void eliminaById(Long id, String username) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiera non trovata"));

        if (!username.equals(entity.getCreatoDa())) {
            throw new SecurityException("Non autorizzato a eliminare questa fiera");
        }

        repository.delete(entity);
        notifier.notificaTutti(mapToDomain(entity), "FIERA_ELIMINATA");
    }

    @Override
    public List<FieraDto> getFiereByStato(StatoEvento stato) {
        return repository.findByStato(stato)
                .stream()
                .map(this::mapToDto)
                .toList();
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
        e.setStato(fiera.getStato() != null ? fiera.getStato() : StatoEvento.PUBBLICATA);
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
                .stato(e.getStato())
                .prezzo(e.getPrezzo())
                .build();
    }

    private FieraDto mapToDto(FieraEntity e) {
        FieraDto dto = new FieraDto();
        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setDescrizione(e.getDescrizione());
        dto.setIndirizzo(e.getIndirizzo());
        dto.setDataInizio(e.getDataInizio());
        dto.setDataFine(e.getDataFine());
        dto.setCreatoDa(e.getCreatoDa());
        dto.setStato(e.getStato());
        dto.setPrezzo(e.getPrezzo());
        dto.setTipo(unicam.filiera.dto.EventoTipo.FIERA);
        return dto;
    }
}
