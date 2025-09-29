package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.factory.ItemFactory;
import unicam.filiera.factory.ProdottoFactory;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.observer.ProdottoNotifier;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.validation.ProdottoValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
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
        // Validazione centralizzata
        ProdottoValidator.valida(dto);

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

        // Validazione centralizzata anche in aggiornamento
        ProdottoValidator.valida(dto);

        Prodotto updated = ProdottoFactory.creaProdotto(dto, creatore);
        updated.setCommento(null);
        updated.setStato(StatoProdotto.IN_ATTESA);

        ProdottoEntity entity = mapToEntity(updated, dto, existing.getId());

        boolean nessunCertNuovo = dto.getCertificati() == null ||
                dto.getCertificati().stream().allMatch(MultipartFile::isEmpty);
        boolean nessunaFotoNuova = dto.getFoto() == null ||
                dto.getFoto().stream().allMatch(MultipartFile::isEmpty);

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
    public List<ProdottoDto> getProdottiCreatiDa(String creatore) {
        return repository.findByCreatoDa(creatore)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<ProdottoDto> getProdottiByStato(StatoProdotto stato) {
        return repository.findByStato(stato)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public Optional<ProdottoEntity> getProdottoById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<ProdottoDto> getProdottiApprovatiByProduttore(String usernameProduttore) {
        return repository.findByStatoAndCreatoDa(StatoProdotto.APPROVATO, usernameProduttore)
                .stream()
                .map(this::mapToDto)
                .toList();
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

    @Override
    public Optional<ProdottoEntity> findEntityById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<ProdottoDto> findDtoById(Long id) {
        return repository.findById(id)
                .map(this::mapToDto); // usa già il mapper interno
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
            // 1. Nome originale
            String original = multipartFile.getOriginalFilename();

            // 2. Rimpiazzo spazi e caratteri strani
            String safeName = original == null ? "file"
                    : original.replaceAll("\\s+", "_")
                    .replaceAll("[^a-zA-Z0-9._-]", "");

            // 3. UUID per evitare conflitti
            String filename = UUID.randomUUID() + "_" + safeName;

            // 4. Salvataggio fisico
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

        // Campi base (dal DTO)
        e.setNome(dto.getNome());
        e.setDescrizione(dto.getDescrizione());
        e.setIndirizzo(dto.getIndirizzo());
        e.setQuantita(dto.getQuantita());
        e.setPrezzo(dto.getPrezzo());

        // Campi business (dal domain Prodotto, che può avere logica di validazione/strategy già applicata)
        e.setCreatoDa(prodotto.getCreatoDa());
        e.setStato(prodotto.getStato());
        e.setCommento(prodotto.getCommento());

        // File gestiti dal DTO
        e.setCertificati(toCsv(dto.getCertificati(), CERT_DIR));
        e.setFoto(toCsv(dto.getFoto(), FOTO_DIR));

        return e;
    }

    private ProdottoDto mapToDto(ProdottoEntity e) {
        ProdottoDto dto = new ProdottoDto();
        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setDescrizione(e.getDescrizione());
        dto.setIndirizzo(e.getIndirizzo());
        dto.setQuantita(e.getQuantita());
        dto.setPrezzo(e.getPrezzo());
        dto.setTipo(ItemTipo.PRODOTTO);

        // Campi specifici di ProdottoDto
        dto.setCreatoDa(e.getCreatoDa());
        dto.setStato(e.getStato());
        dto.setCommento(e.getCommento());

        // Campi di BaseItemDto (già presenti nella superclasse)
        dto.setCertificatiCsv(e.getCertificati());
        dto.setFotoCsv(e.getFoto());

        return dto;
    }

    private Prodotto mapToDomain(ProdottoEntity e) {
        int quantita = e.getQuantita();
        if (quantita < 0) {
            throw new IllegalStateException("La quantità del prodotto non può essere negativa");
        }

        return new Prodotto.Builder()
                .id(e.getId())
                .nome(e.getNome())
                .descrizione(e.getDescrizione())
                .indirizzo(e.getIndirizzo())
                .quantita(quantita)
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
