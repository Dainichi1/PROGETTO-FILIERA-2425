package unicam.filiera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.PacchettoViewDto;
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
import java.nio.file.StandardCopyOption;
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
    public void aggiornaPacchetto(Long id, PacchettoDto dto, String creatore) {
        PacchettoEntity existing = pacchettoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato per la modifica"));

        if (!existing.getCreatoDa().equals(creatore)) {
            throw new SecurityException("Non autorizzato a modificare questo pacchetto");
        }

        if (existing.getStato() != StatoProdotto.RIFIUTATO) {
            throw new IllegalStateException("Puoi modificare solo pacchetti con stato RIFIUTATO");
        }

        Pacchetto updated = PacchettoFactory.creaPacchetto(dto, creatore);
        updated.setCommento(null);
        updated.setStato(StatoProdotto.IN_ATTESA);

        PacchettoEntity entity = mapToEntity(updated, dto, existing.getId());

        // === PRESERVAZIONE FILE: se non sono stati caricati nuovi file, mantieni quelli vecchi ===
        boolean nessunCertNuovo = dto.getCertificati() == null || dto.getCertificati().stream().allMatch(MultipartFile::isEmpty);
        boolean nessunaFotoNuova = dto.getFoto() == null || dto.getFoto().stream().allMatch(MultipartFile::isEmpty);

        if (nessunCertNuovo) {
            entity.setCertificati(existing.getCertificati());
        }
        if (nessunaFotoNuova) {
            entity.setFoto(existing.getFoto());
        }

        pacchettoRepository.save(entity);

        notifier.notificaTutti(updated, "NUOVO_PACCHETTO");
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
    public void eliminaPacchettoById(Long id, String creatore) {
        PacchettoEntity entity = pacchettoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato"));

        if (!entity.getCreatoDa().equals(creatore)) {
            throw new SecurityException("Non autorizzato a eliminare questo pacchetto");
        }
        if (entity.getStato() == StatoProdotto.APPROVATO) {
            throw new IllegalStateException("Non puoi eliminare un pacchetto già approvato");
        }

        Pacchetto pacchetto = mapToDomain(entity);
        pacchettoRepository.delete(entity);

        notifier.notificaTutti(pacchetto, "ELIMINATO_PACCHETTO");
    }

    @Override
    public void cambiaStatoPacchetto(String nome, String creatore, StatoProdotto nuovoStato, String commento) {
        PacchettoEntity entity = pacchettoRepository.findByNomeAndCreatoDa(nome, creatore)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato"));

        aggiornaStatoECommento(entity, nuovoStato, commento);
        pacchettoRepository.save(entity);

        Pacchetto pacchetto = mapToDomain(entity);
        notifier.notificaTutti(pacchetto,
                nuovoStato == StatoProdotto.APPROVATO ? "APPROVATO" : "RIFIUTATO");
    }


    public Optional<PacchettoEntity> findEntityById(Long id) {
        return pacchettoRepository.findById(id);
    }

    @Override
    public List<PacchettoViewDto> getPacchettiViewByStato(StatoProdotto stato) {
        return pacchettoRepository.findByStato(stato).stream()
                .map(e -> {
                    List<String> prodottiNomi = e.getProdotti() == null ? List.of() :
                            e.getProdotti().stream()
                                    .map(ProdottoEntity::getNome)
                                    .toList();

                    return new PacchettoViewDto(
                            e.getId(),
                            e.getNome(),
                            e.getDescrizione(),
                            e.getQuantita(),
                            e.getPrezzo(),
                            e.getIndirizzo(),
                            e.getCreatoDa(),
                            e.getStato().name(),
                            e.getCommento(),
                            e.getCertificati() == null || e.getCertificati().isBlank()
                                    ? List.of()
                                    : List.of(e.getCertificati().split(",")),
                            e.getFoto() == null || e.getFoto().isBlank()
                                    ? List.of()
                                    : List.of(e.getFoto().split(",")),
                            prodottiNomi
                    );
                })
                .toList();
    }

    @Override
    public List<PacchettoViewDto> getPacchettiViewByCreatore(String creatore) {
        return pacchettoRepository.findByCreatoDa(creatore).stream()
                .map(e -> {
                    List<String> prodottiNomi = e.getProdotti() == null ? List.of() :
                            e.getProdotti().stream()
                                    .map(ProdottoEntity::getNome)
                                    .toList();

                    return new PacchettoViewDto(
                            e.getId(),
                            e.getNome(),
                            e.getDescrizione(),
                            e.getQuantita(),
                            e.getPrezzo(),
                            e.getIndirizzo(),
                            e.getCreatoDa(),
                            e.getStato().name(),
                            e.getCommento(),
                            e.getCertificati() == null || e.getCertificati().isBlank()
                                    ? List.of()
                                    : List.of(e.getCertificati().split(",")),
                            e.getFoto() == null || e.getFoto().isBlank()
                                    ? List.of()
                                    : List.of(e.getFoto().split(",")),
                            prodottiNomi
                    );
                })
                .toList();
    }



    // =======================
    // Helpers
    // =======================
    private void aggiornaStatoECommento(PacchettoEntity entity, StatoProdotto nuovoStato, String commento) {
        entity.setStato(nuovoStato);
        if (nuovoStato == StatoProdotto.RIFIUTATO) {
            entity.setCommento((commento != null && !commento.isBlank()) ? commento : null);
        } else {
            entity.setCommento(null);
        }
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
        return files == null ? "" :
                files.stream()
                        .filter(f -> f != null && !f.isEmpty())
                        .map(file -> salvaMultipartFile(file, dir))
                        .collect(Collectors.joining(","));
    }

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

        // Prodotti selezionati
        Set<ProdottoEntity> prodotti = dto.getProdottiSelezionati() == null ? Set.of() :
                dto.getProdottiSelezionati().stream()
                        .map(prodottoRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
        e.setProdotti(prodotti);

        e.setCertificati(toCsv(dto.getCertificati(), CERT_DIR));
        e.setFoto(toCsv(dto.getFoto(), FOTO_DIR));

        return e;
    }

    private Pacchetto mapToDomain(PacchettoEntity e) {
        return new Pacchetto.Builder()
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
                .prodottiIds(e.getProdotti() == null
                        ? List.of()
                        : e.getProdotti().stream()
                        .map(ProdottoEntity::getId)
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }

}
