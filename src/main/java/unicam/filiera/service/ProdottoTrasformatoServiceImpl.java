package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.entity.FaseProduzioneEmbeddable;
import unicam.filiera.entity.ProdottoTrasformatoEntity;
import unicam.filiera.factory.ItemFactory;
import unicam.filiera.factory.ProdottoTrasformatoFactory;
import unicam.filiera.model.FaseProduzione;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.observer.ProdottoTrasformatoNotifier;
import unicam.filiera.repository.ProdottoTrasformatoRepository;

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
public class ProdottoTrasformatoServiceImpl implements ProdottoTrasformatoService {

    private static final String CERT_DIR = "uploads/certificati";
    private static final String FOTO_DIR = "uploads/foto";

    private final ProdottoTrasformatoRepository repository;
    private final ProdottoTrasformatoNotifier notifier;

    @Autowired
    public ProdottoTrasformatoServiceImpl(ProdottoTrasformatoRepository repository,
                                          ProdottoTrasformatoNotifier notifier) {
        this.repository = repository;
        this.notifier = notifier;

        new File(CERT_DIR).mkdirs();
        new File(FOTO_DIR).mkdirs();
    }

    @Override
    public void creaProdottoTrasformato(ProdottoTrasformatoDto dto, String creatore) {
        ProdottoTrasformato prodottoTrasformato = ItemFactory.creaProdottoTrasformato(dto, creatore);

        ProdottoTrasformatoEntity entity = mapToEntity(prodottoTrasformato, dto, null);
        repository.save(entity);

        notifier.notificaTutti(prodottoTrasformato, "NUOVO_PRODOTTO_TRASFORMATO");
    }

    @Override
    public void aggiornaProdottoTrasformato(String nomeOriginale, ProdottoTrasformatoDto dto, String creatore) {
        ProdottoTrasformatoEntity existing = repository.findByNomeAndCreatoDa(nomeOriginale, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto trasformato non trovato per la modifica"));

        if (existing.getStato() != StatoProdotto.RIFIUTATO) {
            throw new IllegalStateException("Puoi modificare solo prodotti trasformati con stato RIFIUTATO");
        }

        ProdottoTrasformato updated = ProdottoTrasformatoFactory.creaProdottoTrasformato(dto, creatore);
        updated.setCommento(null);
        updated.setStato(StatoProdotto.IN_ATTESA);

        ProdottoTrasformatoEntity entity = mapToEntity(updated, dto, existing.getId());
        repository.save(entity);

        notifier.notificaTutti(updated, "NUOVO_PRODOTTO_TRASFORMATO");
    }

    @Override
    public List<ProdottoTrasformato> getProdottiTrasformatiCreatiDa(String creatore) {
        return repository.findByCreatoDa(creatore)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProdottoTrasformato> getProdottiTrasformatiByStato(StatoProdotto stato) {
        return repository.findByStato(stato)
                .stream()
                .filter(e -> e.getFasiProduzione() != null && e.getFasiProduzione().size() >= 2)
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminaProdottoTrasformato(String nome, String creatore) {
        ProdottoTrasformatoEntity entity = repository.findByNomeAndCreatoDa(nome, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto trasformato non trovato"));

        if (entity.getStato() == StatoProdotto.APPROVATO) {
            throw new IllegalStateException("Non puoi eliminare un prodotto trasformato già approvato");
        }

        repository.delete(entity);
        notifier.notificaTutti(mapToDomain(entity), "ELIMINATO_PRODOTTO_TRASFORMATO");
    }

    @Override
    public void cambiaStatoProdottoTrasformato(String nome, String creatore, StatoProdotto nuovoStato, String commento) {
        ProdottoTrasformatoEntity entity = repository.findByNomeAndCreatoDa(nome, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto trasformato non trovato"));

        aggiornaStatoECommento(entity, nuovoStato, commento);

        repository.save(entity);

        ProdottoTrasformato prodotto = mapToDomain(entity);
        notifier.notificaTutti(
                prodotto,
                nuovoStato == StatoProdotto.APPROVATO ? "APPROVATO" : "RIFIUTATO"
        );
    }

    // =======================
    // Mapping Helpers
    // =======================

    private ProdottoTrasformatoEntity mapToEntity(ProdottoTrasformato prodotto, ProdottoTrasformatoDto dto, Long id) {
        ProdottoTrasformatoEntity e = new ProdottoTrasformatoEntity();
        e.setId(id);
        e.setNome(prodotto.getNome());
        e.setDescrizione(prodotto.getDescrizione());
        e.setIndirizzo(prodotto.getIndirizzo());
        e.setQuantita(prodotto.getQuantita());
        e.setPrezzo(prodotto.getPrezzo());
        e.setCreatoDa(prodotto.getCreatoDa());
        e.setStato(prodotto.getStato());
        e.setCommento(prodotto.getCommento());

        // usa helper per le fasi
        e.setFasiProduzione(toEmbeddableList(prodotto.getFasiProduzione()));

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

    private ProdottoTrasformato mapToDomain(ProdottoTrasformatoEntity e) {
        return new ProdottoTrasformato.Builder()
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
                .fasiProduzione(toDomainList(e.getFasiProduzione()))
                .build();
    }

    // Conversion Helpers per le fasi
    private List<FaseProduzioneEmbeddable> toEmbeddableList(List<FaseProduzione> fasi) {
        return fasi == null ? List.of() :
                fasi.stream()
                        .map(f -> new FaseProduzioneEmbeddable(
                                f.getDescrizioneFase(),
                                f.getProduttoreUsername(),
                                f.getProdottoOrigineId()
                        ))
                        .collect(Collectors.toList());
    }

    private List<FaseProduzione> toDomainList(List<FaseProduzioneEmbeddable> fasi) {
        return fasi == null ? List.of() :
                fasi.stream()
                        .map(f -> new FaseProduzione(
                                f.getDescrizioneFase(),
                                f.getProduttoreUsername(),
                                f.getProdottoOrigineId()
                        ))
                        .collect(Collectors.toList());
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
    // Stato Helper
    // =======================

    private void aggiornaStatoECommento(ProdottoTrasformatoEntity entity, StatoProdotto nuovoStato, String commento) {
        entity.setStato(nuovoStato);
        if (nuovoStato == StatoProdotto.RIFIUTATO) {
            entity.setCommento((commento != null && !commento.isBlank()) ? commento : null);
        } else {
            entity.setCommento(null);
        }
    }
}
