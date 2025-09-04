package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.factory.ItemFactory;
import unicam.filiera.factory.ProdottoFactory;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.observer.ProdottoNotifier;
import unicam.filiera.repository.ProdottoRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProdottoServiceImpl implements ProdottoService {

    private static final String CERT_DIR = "uploads/certificati";
    private static final String FOTO_DIR = "uploads/foto";

    private final ProdottoRepository repository;
    private final ProdottoNotifier notifier;

    @Autowired
    public ProdottoServiceImpl(ProdottoRepository repository, ProdottoNotifier notifier) {
        this.repository = repository;
        this.notifier = notifier;

        // crea le cartelle se non esistono
        new File(CERT_DIR).mkdirs();
        new File(FOTO_DIR).mkdirs();
    }

    @Override
    public void creaProdotto(ProdottoDto dto, String creatore) {
        Prodotto prodotto = ItemFactory.creaProdotto(dto, creatore);

        ProdottoEntity entity = mapToEntity(prodotto, dto, null);
        repository.save(entity);

        notifier.notificaTutti(prodotto, "NUOVO_PRODOTTO");
    }

    @Override
    public void aggiornaProdotto(String nomeOriginale, ProdottoDto dto, String creatore) {
        ProdottoEntity existing = repository.findByNomeAndCreatoDa(nomeOriginale, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato per la modifica"));

        if (existing.getStato() != StatoProdotto.RIFIUTATO) {
            throw new IllegalStateException("Puoi modificare solo prodotti con stato RIFIUTATO");
        }

        Prodotto updated = ProdottoFactory.creaProdotto(dto, creatore);
        updated.setCommento(null);
        updated.setStato(StatoProdotto.IN_ATTESA);

        ProdottoEntity entity = mapToEntity(updated, dto, existing.getId());
        repository.save(entity);

        notifier.notificaTutti(updated, "NUOVO_PRODOTTO");
    }

    @Override
    public List<Prodotto> getProdottiCreatiDa(String creatore) {
        return repository.findByCreatoDa(creatore)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Prodotto> getProdottiByStato(StatoProdotto stato) {
        return repository.findByStato(stato)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Prodotto> getProdottiApprovatiByProduttore(String usernameProduttore) {
        return repository.findByStatoAndCreatoDa(StatoProdotto.APPROVATO, usernameProduttore)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminaProdotto(String nome, String creatore) {
        ProdottoEntity entity = repository.findByNomeAndCreatoDa(nome, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));

        if (entity.getStato() == StatoProdotto.APPROVATO) {
            throw new IllegalStateException("Non puoi eliminare un prodotto già approvato");
        }

        repository.delete(entity);
        notifier.notificaTutti(mapToDomain(entity), "ELIMINATO_PRODOTTO");
    }

    @Override
    public void cambiaStatoProdotto(String nome, String creatore, StatoProdotto nuovoStato, String commento) {
        ProdottoEntity entity = repository.findByNomeAndCreatoDa(nome, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));

        aggiornaStatoECommento(entity, nuovoStato, commento);

        repository.save(entity);

        Prodotto prodotto = mapToDomain(entity);
        notifier.notificaTutti(
                prodotto,
                nuovoStato == StatoProdotto.APPROVATO ? "APPROVATO" : "RIFIUTATO"
        );
    }

    // =======================
    // Stato Helper
    // =======================

    private void aggiornaStatoECommento(ProdottoEntity entity, StatoProdotto nuovoStato, String commento) {
        entity.setStato(nuovoStato);
        if (nuovoStato == StatoProdotto.RIFIUTATO) {
            entity.setCommento((commento != null && !commento.isBlank()) ? commento : null);
        } else {
            entity.setCommento(null);
        }
    }

    // =======================
    // File Helper
    // =======================

    private String salvaMultipartFile(MultipartFile multipartFile, String destDir) {
        try {
            String filename = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
            Path path = Paths.get(destDir, filename);
            Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Errore nel salvataggio del file " + multipartFile.getOriginalFilename(), e);
        }
    }

    // =======================
    // Mapping Helpers
    // =======================

    private ProdottoEntity mapToEntity(Prodotto prodotto, ProdottoDto dto, Long id) {
        ProdottoEntity e = new ProdottoEntity();
        e.setId(id);
        e.setNome(prodotto.getNome());
        e.setDescrizione(prodotto.getDescrizione());
        e.setIndirizzo(prodotto.getIndirizzo());
        e.setQuantita(prodotto.getQuantita());
        e.setPrezzo(prodotto.getPrezzo());
        e.setCreatoDa(prodotto.getCreatoDa());
        e.setStato(prodotto.getStato());
        e.setCommento(prodotto.getCommento());

        e.setCertificati(toCsv(dto.getCertificati(), CERT_DIR));
        e.setFoto(toCsv(dto.getFoto(), FOTO_DIR));

        return e;
    }

    private Prodotto mapToDomain(ProdottoEntity e) {
        return new Prodotto.Builder()
                .id(e.getId())
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
                .build();
    }

    // helper per conversione fileList → CSV
    private String toCsv(List<MultipartFile> files, String dir) {
        return files == null ? "" :
                files.stream()
                        .map(file -> salvaMultipartFile(file, dir))
                        .collect(Collectors.joining(","));
    }
}
