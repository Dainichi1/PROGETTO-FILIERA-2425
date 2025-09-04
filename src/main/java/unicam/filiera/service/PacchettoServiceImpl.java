package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.factory.ItemFactory;
import unicam.filiera.factory.PacchettoFactory;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.observer.PacchettoNotifier;
import unicam.filiera.repository.PacchettoRepository;
import unicam.filiera.repository.ProdottoRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PacchettoServiceImpl implements PacchettoService {

    private static final String CERT_DIR = "uploads/certificati";
    private static final String FOTO_DIR = "uploads/foto";

    private final PacchettoRepository pacchettoRepository;
    private final ProdottoRepository prodottoRepository;
    private final PacchettoNotifier notifier;

    @Autowired
    public PacchettoServiceImpl(PacchettoRepository pacchettoRepository,
                                ProdottoRepository prodottoRepository,
                                PacchettoNotifier notifier) {
        this.pacchettoRepository = pacchettoRepository;
        this.prodottoRepository = prodottoRepository;
        this.notifier = notifier;

        // crea le cartelle se non esistono
        new File(CERT_DIR).mkdirs();
        new File(FOTO_DIR).mkdirs();
    }

    @Override
    public void creaPacchetto(PacchettoDto dto, String creatore) {
        Pacchetto pacchetto = ItemFactory.creaPacchetto(dto, creatore);

        PacchettoEntity entity = mapToEntity(pacchetto, dto, null);
        pacchettoRepository.save(entity);

        notifier.notificaTutti(pacchetto, "NUOVO_PACCHETTO");
    }

    @Override
    public List<Pacchetto> getPacchettiCreatiDa(String creatore) {
        return pacchettoRepository.findByCreatoDa(creatore)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pacchetto> getPacchettiByStato(StatoProdotto stato) {
        return pacchettoRepository.findByStato(stato)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminaPacchetto(String nome, String creatore) {
        PacchettoEntity entity = pacchettoRepository.findByNomeAndCreatoDa(nome, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato"));

        if (entity.getStato() == StatoProdotto.APPROVATO) {
            throw new IllegalStateException("Non puoi eliminare un pacchetto già approvato");
        }

        pacchettoRepository.delete(entity);
        notifier.notificaTutti(mapToDomain(entity), "ELIMINATO_PACCHETTO");
    }

    @Override
    public void aggiornaPacchetto(String nomeOriginale, PacchettoDto dto, String creatore) {
        PacchettoEntity existing = pacchettoRepository.findByNomeAndCreatoDa(nomeOriginale, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato per la modifica"));

        if (existing.getStato() != StatoProdotto.RIFIUTATO) {
            throw new IllegalStateException("Puoi modificare solo pacchetti con stato RIFIUTATO");
        }

        Pacchetto updated = PacchettoFactory.creaPacchetto(dto, creatore);
        updated.setCommento(null);
        updated.setStato(StatoProdotto.IN_ATTESA);

        PacchettoEntity entity = mapToEntity(updated, dto, existing.getId());
        pacchettoRepository.save(entity);

        notifier.notificaTutti(updated, "NUOVO_PACCHETTO");
    }

    @Override
    public void cambiaStatoPacchetto(String nome, String creatore, StatoProdotto nuovoStato, String commento) {
        PacchettoEntity entity = pacchettoRepository.findByNomeAndCreatoDa(nome, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato"));

        entity.setStato(nuovoStato);

        if (nuovoStato == StatoProdotto.RIFIUTATO) {
            entity.setCommento((commento != null && !commento.isBlank()) ? commento : null);
        } else {
            entity.setCommento(null);
        }

        pacchettoRepository.save(entity);

        Pacchetto pacchetto = mapToDomain(entity);
        notifier.notificaTutti(
                pacchetto,
                nuovoStato == StatoProdotto.APPROVATO ? "APPROVATO" : "RIFIUTATO"
        );
    }

    // =======================
    // Mapping Helpers
    // =======================

    private PacchettoEntity mapToEntity(Pacchetto pacchetto, PacchettoDto dto, Long id) {
        PacchettoEntity e = new PacchettoEntity();
        e.setId(id);
        e.setNome(pacchetto.getNome());
        e.setDescrizione(pacchetto.getDescrizione());
        e.setIndirizzo(pacchetto.getIndirizzo());
        e.setQuantita(pacchetto.getQuantita());
        e.setPrezzo(pacchetto.getPrezzo());
        e.setCreatoDa(pacchetto.getCreatoDa());
        e.setStato(pacchetto.getStato());
        e.setCommento(pacchetto.getCommento());

        // Associa i prodotti selezionati
        Set<ProdottoEntity> prodotti = dto.getProdottiSelezionati() == null ? Set.of() :
                dto.getProdottiSelezionati().stream()
                        .map(prodottoRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
        e.setProdotti(prodotti);

        // Salvataggio fisico file
        String certCsv = dto.getCertificati() == null ? "" :
                dto.getCertificati().stream()
                        .map(file -> salvaMultipartFile(file, CERT_DIR))
                        .collect(Collectors.joining(","));

        String fotoCsv = dto.getFoto() == null ? "" :
                dto.getFoto().stream()
                        .map(file -> salvaMultipartFile(file, FOTO_DIR))
                        .collect(Collectors.joining(","));

        e.setCertificati(certCsv);
        e.setFoto(fotoCsv);

        return e;
    }

    private Pacchetto mapToDomain(PacchettoEntity e) {
        return new Pacchetto.Builder()
                .nome(e.getNome())
                .descrizione(e.getDescrizione())
                .indirizzo(e.getIndirizzo())
                .quantita(e.getQuantita())
                .prezzo(e.getPrezzo())
                .creatoDa(e.getCreatoDa())
                .stato(e.getStato())
                .commento(e.getCommento())
                .certificati(e.getCertificati() == null || e.getCertificati().isBlank()
                        ? List.of()
                        : List.of(e.getCertificati().split(",")))
                .foto(e.getFoto() == null || e.getFoto().isBlank()
                        ? List.of()
                        : List.of(e.getFoto().split(",")))
                .prodotti(e.getProdotti() == null
                        ? List.of()
                        : e.getProdotti().stream()
                        .map(ProdottoEntity::getNome)
                        .collect(Collectors.toList()))
                .build();
    }

    // =======================
    // File Helper
    // =======================

    private String salvaMultipartFile(MultipartFile multipartFile, String destDir) {
        try {
            String filename = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
            Path path = Paths.get(destDir, filename);
            Files.copy(multipartFile.getInputStream(), path);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Errore nel salvataggio del file " + multipartFile.getOriginalFilename(), e);
        }
    }
}
