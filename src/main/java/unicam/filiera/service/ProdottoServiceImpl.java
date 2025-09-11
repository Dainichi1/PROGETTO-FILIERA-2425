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
import java.util.Optional; // <-- import
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
    public void aggiornaProdotto(Long id, ProdottoDto dto, String creatore) {
        ProdottoEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato per la modifica"));

        if (!existing.getCreatoDa().equals(creatore)) {
            throw new SecurityException("Non autorizzato a modificare questo prodotto");
        }
        if (existing.getStato() != StatoProdotto.RIFIUTATO) {
            throw new IllegalStateException("Puoi modificare solo prodotti con stato RIFIUTATO");
        }

        Prodotto updated = ProdottoFactory.creaProdotto(dto, creatore);
        updated.setCommento(null);
        updated.setStato(StatoProdotto.IN_ATTESA);

        // Mappo dai nuovi dati
        ProdottoEntity entity = mapToEntity(updated, dto, existing.getId());

        // === PRESERVAZIONE FILE: se non sono stati ricaricati, mantieni quelli esistenti ===
        boolean nessunCertNuovo = dto.getCertificati()==null || dto.getCertificati().stream().allMatch(MultipartFile::isEmpty);
        boolean nessunaFotoNuova = dto.getFoto()==null || dto.getFoto().stream().allMatch(MultipartFile::isEmpty);

        if (nessunCertNuovo) {
            entity.setCertificati(existing.getCertificati());
        }
        if (nessunaFotoNuova) {
            entity.setFoto(existing.getFoto());
        }

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
    public void cambiaStatoProdotto(String nome, String creatore, StatoProdotto nuovoStato, String commento) {
        ProdottoEntity entity = repository.findByNomeAndCreatoDa(nome, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));

        aggiornaStatoECommento(entity, nuovoStato, commento);
        repository.save(entity);

        Prodotto prodotto = mapToDomain(entity);
        notifier.notificaTutti(prodotto,
                nuovoStato == StatoProdotto.APPROVATO ? "APPROVATO" : "RIFIUTATO");
    }

    // === NUOVO: finder per l'entity (serve per prefill JSON) ===
    @Override
    public Optional<ProdottoEntity> findEntityById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void eliminaById(Long id, String username) {
        ProdottoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));

        if (!username.equals(entity.getCreatoDa())) {
            throw new SecurityException("Non autorizzato a eliminare questo prodotto");
        }
        if (entity.getStato() == StatoProdotto.APPROVATO) {
            throw new IllegalStateException("Si possono eliminare solo prodotti IN_ATTESA o RIFIUTATO");
        }

        Prodotto dominio = mapToDomain(entity);
        repository.delete(entity);
        notifier.notificaTutti(dominio, "ELIMINATO_PRODOTTO");
    }

    // =======================
    // Helpers
    // =======================

    private void aggiornaStatoECommento(ProdottoEntity entity, StatoProdotto nuovoStato, String commento) {
        entity.setStato(nuovoStato);
        entity.setCommento(nuovoStato == StatoProdotto.RIFIUTATO
                ? (commento != null && !commento.isBlank() ? commento : null)
                : null);
    }

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

    private String toCsv(List<MultipartFile> files, String dir) {
        return (files == null || files.isEmpty())
                ? null
                : files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(file -> salvaMultipartFile(file, dir))
                .collect(Collectors.joining(","));
    }

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
}
